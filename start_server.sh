if [ $# -lt 5 ]
then
	echo "Syntax: $0 <bot1_name> <bot1_path> <bot2_name> <bot2_path>"
	exit 1
fi

BOT1_NAME=$1
BOT1_PATH=$2
BOT2_NAME=$3
BOT2_PATH=$4

CLIENT_LIBS=mm19/lib/JSON-java-master.jar
SERVER_LIBS=$CLIENT_LIBS:mm19/lib/jasypt-1.9.0.jar

BOT_MAIN_CLASS=mm19.runner.TestClientRunner

java -cp mm19/bin:$SERVER_LIBS mm19.server.Server &
sleep 2
java -cp $BOT1_PATH/bin:$CLIENT_LIBS $BOT_MAIN_CLASS $BOT1_NAME &
java -cp $BOT2_PATH/bin:$CLIENT_LIBS $BOT_MAIN_CLASS $BOT2_NAME &
fg 1
