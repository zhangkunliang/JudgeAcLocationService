#!/bin/sh

PROGRAM='NLPExtractionService'
SET_DIR="/home/NetPF/bin/$PROGRAM"
JAVA_FILE="/usr/java/java8"
GUARG_FILE="fudeguard-netpf-NLPExtractionService.xml"
WORK_DIR=`pwd`
#安装类型
INSTALL_TYPE=$1

#安装程序版本
VERSION=`cat release-note  | grep '^Version' | cut -d= -f2`

#fude守护xml文件存放目录
if [ -z $FUDE_ROOT ]; then
	FUDE_ROOT=/opt/FUDE
fi
GUARD_DIR=$FUDE_ROOT/fude/etc/guard/conf.d

#colour level
SETCOLOR_SUCCESS="echo -en \\033[1;32m"
SETCOLOR_FAILURE="echo -en \\033[1;31m"
SETCOLOR_WARNING="echo -en \\033[1;33m"
SETCOLOR_NORMAL="echo -en \\033[0;39m"
#定义记录日志的函数
writeLog(){
    # print time
    time=`date "+%D %T"`
    echo "[$time] : $PROGRAM-INSTALL: INFO     : $*"
}

LogWarnMsg()
{
	time=`date "+%D %T"`
	$SETCOLOR_WARNING
	echo "[$time] : $PROGRAM-INSTALL: WARN     : $*"
	$SETCOLOR_NORMAL
}

LogSucMsg()
{
	time=`date "+%D %T"`
	$SETCOLOR_SUCCESS
	echo "[$time] : $PROGRAM-INSTALL: SUCCESS : $*"
	$SETCOLOR_NORMAL
}

LogErrorMsg()
{
	time=`date "+%D %T"`
	$SETCOLOR_FAILURE
	echo "[$time] : $PROGRAM-INSTALL: ERROR   : $*"
	$SETCOLOR_NORMAL
}

check_guard()
{
	if [ ! -d $GUARD_DIR ]
	then
		echo "Error! not found guard install! please install Guard first!"
		exit
	fi
}

check_java()
{
	if [ ! -f $JAVA_FILE/bin/java ]
	then
		echo "Error! not found java 1.8 ! please install jdk 1.8 version first!!"
		exit
	fi
}

#检测jdk 以及配置项
check_env()
{
  check_java
	check_guard
}


##备份原目录
backup(){
	#判断SET_DIR目录是否存在，将SET_DIR目录bak，停掉服务，防止安装目录有修改但有已运行相关程序
	if [ -d $SET_DIR ]; then
		#已安装程序版本文件
		OLD_VERSION_NOTE=$SET_DIR/release-note
		if [ -f $OLD_VERSION_NOTE ]; then
			sed -i "s/\r//" $OLD_VERSION_NOTE
			
			#已安装程序版本
			OLD_VERSION=`cat $OLD_VERSION_NOTE  | grep '^Version' | cut -d= -f2`
			
			if [ $VERSION = $OLD_VERSION ]; then
				LogWarnMsg "$PROGRAM $VERSION already install!"
				exit 1
			fi
		fi
		
		#停止服务
		stopProcess
		
		#bak掉目录
		timeStamp=`date "+%Y%m%d%H%M%S"`
		BACKUP_DIR=${SET_DIR}_bak_${timeStamp}
		mv ${SET_DIR} ${BACKUP_DIR}
	fi
}

installation()
{
  #初始化
	#备份原目录
	backup
	
	alias cp='cp'
	cd $WORK_DIR
#if there is a config file,copy it
	rm -rf "$SET_DIR"
	mkdir -p "$SET_DIR"
	cp -rf * "$SET_DIR"

	cd "$SET_DIR"
	rm -rf *.tar.gz
	rm -rf install.sh
	#dos2unix bin/Run.sh
	#dos2unix conf/*.properties
	chmod a+x bin/Run.sh
	cp -rf $GUARG_FILE $GUARD_DIR
	. /opt/FUDE/fude/profile/fude_profile
	fudeguardmgr.py --reload
}

##停止服务
stopProcess(){
	#删除FUDE配置
  if [ -d $GUARD_DIR ]; then
      writeLog "INFO Deleting $GUARG_FILE"
      rm -rf $GUARD_DIR/$GUARG_FILE
      sleep 5
      . /opt/FUDE/fude/profile/fude_profile
      fudeguardmgr.py --re
  fi
	#停止服务
	if [ -d $SET_DIR ]; then
	   cd $SET_DIR
	   sh bin/Run.sh stop
	fi
	
	cd $WORK_DIR
}
##卸载
uninstallation(){
	#停止服务
	stopProcess
	
	#删除安装目录
  rm -rf $SET_DIR
}

usage()
{
writeLog "-------------------------------------"
writeLog "input error ! You must input params!!!"
writeLog "sh install.sh <INSTALL_TYPE>"
writeLog "sh install.sh install     Install the application"
writeLog "sh install.sh uninstall   UnInstall the application"
writeLog "sh install.sh reinstall   ReInstall the application"
}

case "$INSTALL_TYPE" in
	install)
		#开始安装
		check_env
    LogSucMsg "$PROGRAM env_check success!"
	  installation
		LogSucMsg "$PROGRAM install success!"
		;;
	uninstall)
		#卸载
	  uninstallation
		LogSucMsg "uninstall the $PROGRAM success!"
		;;
	reinstall)
	  #重新安装
    uninstallation
    LogSucMsg "uninstall the $PROGRAM success!"
	  check_env
    LogSucMsg "$PROGRAM env_check success!"
		installation
		LogSucMsg "$PROGRAM reinstall success!"
		;;
	*)
		usage
		exit 1
esac
