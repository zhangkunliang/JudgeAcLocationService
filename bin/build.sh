#! /bin/sh

if [ ! -d build ];then
  mkdir -p build
else
  rm -rf ./build/*
fi

if [ ! -d package ];then
  mkdir -p package
else
  rm -rf ./package/*
fi

WORK_DIR=`pwd`

#下载代码
svn checkout --username yinxin --password yinxin https://10.1.18.152/svn/Crawler/SpiderX/NLPExtractionService ./build
rm -rf ./build/.svn

dos2unix $WORK_DIR/build/bin/*.sh
dos2unix $WORK_DIR/build/src/main/resources/release-note
dos2unix $WORK_DIR/build/src/main/resources/*.properties
dos2unix $WORK_DIR/build/src/main/resources/*.xml

#maven 打jar
cd ./build/
mvn clean package -Dmaven.test.skip=true
cd ../

version=`cat $WORK_DIR/build/src/main/resources/release-note | grep ^Version= | cut -d= -f2`
moduleName=NLPExtractionService
packageDirName=$moduleName-install-runtime-$version-allsystem-$(date +%Y%m%d)
packageName=$packageDirName.tar.gz
cd $WORK_DIR/build
mv $moduleName-install $packageDirName

tar -zcvf $packageName $packageDirName
md5Sum $packageName > $packageName.md5

#移动打包文件到package
mv $packageName $WORK_DIR/package/
mv $packageName.md5 $WORK_DIR/package/

cp $WORK_DIR/build/src/main/resources/release-note $WORK_DIR/package/
cp $WORK_DIR/build/doc/*.docx $WORK_DIR/package/

rm -rf $WORK_DIR/build/*