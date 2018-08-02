app=xxkg
zone=$1

if [ $1 -lt 10 ]; then
	port=500$1
elif [ $1 -lt 100 ]; then
	port=50$1
else
	port=5$1
fi

mkdir -p ./pub/${app}${zone}/${app}/configs

cp ${app}/target/${app}-1.0.jar ./pub/${app}${zone}/${app}.jar
cp -fr ${app}/configs/* ./pub/${app}${zone}/${app}/configs/

cd ./pub/${app}${zone}

java -XX:+UseG1GC -Xmx300m -Xms32m -jar ${app}.jar --port=${port} --zone=${zone} --profile=007 --script=../commands
