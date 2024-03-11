#! /bin/sh

APP_NAME="belongingplaceservice"
WORK_HOME="/home/NetPF/bin/$APP_NAME"
version=`cat $WORK_HOME/release-note | grep ^Version= | cut -d= -f2`
APP_VERSION="$version"
AUTHOR="X8707"
ABOUT="$APP_NAME Module"
MAIN_CLASS="com.fh.crawler.belongingplaceservice.BelongingPlaceServiceApplication"
JVM_OPTION="-Xms256m -Xmx4096m"
JAVA_FILE="/usr/java/java8"

WORK_DIR=`pwd`	
init()		
{
	export JAVA_HOME=$JAVA_FILE
	export PATH=$JAVA_HOME/bin:$PATH
	export LANG=zh_CN.utf-8
}
start()		
{
	cd $WORK_HOME	
	init			
	
	APPPATH=.		
	
	JARPATH=$APPPATH/lib	
	CONFPATH=$APPPATH/config
	
	LINE=`find $JARPATH -name "*.jar"`
	
	LIBPATH=$CONFPATH	
	
	for LOOP in $LINE	
	do
		LIBPATH=$LIBPATH:$LOOP
	done
	
	exec $JAVA_HOME/bin/java $JVM_OPTION -cp "$APP_NAME:$LIBPATH" $MAIN_CLASS &
}

stop()	
{
	APP_PIDS=`ps -ef --width 4096|grep $APP_NAME |grep -v grep |awk '{print $2}'`
	for LOOP in $APP_PIDS
	do
	        kill -9 $LOOP
	done		
}

showstate()
{
	ps -ef --width 4096 | grep $APP_NAME | grep -v "grep" | grep -v "UrlMerge.sh"
}
showversion()
{
  echo -e "Name:\t\t$APP_NAME"
	echo -e "version:\t$APP_VERSION"
	echo -e "Author:\t\t$AUTHOR"
	echo -e "About:\t\t$ABOUT"
}

case "$1" in	
    start)
    	start $2
		echo -e "$APP_NAME Starting...\t[OK]"
		;;
    stop)
    	stop
		echo -e "$APP_NAME Stopping...\t[OK]"
		;;
    restart)
    	stop
	sleep 2
    	start
        echo -e "$APP_NAME Restarting...\t[OK]"
		;;
    version|-v)
    	showversion
		;;
	state)
		showstate
		;;
    *)
    	echo "Usage: $0 {start|stop|restart|version|state}"
    exit 1
esac

cd $WORK_DIR	

exit 0
