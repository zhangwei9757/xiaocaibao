app=xxkg
zone=$1
db=$2

if [ $1 -lt 10 ]; then
	port=600$1
elif [ $1 -lt 100 ]; then
	port=60$1
else
	port=6$1
fi

mkdir -p ./pub/${app}${zone}/${app}/configs

cp ${app}/target/${app}-1.0.jar ./pub/${app}${zone}/${app}.jar
cp -fr ${app}/configs/* ./pub/${app}${zone}/${app}/configs/

cd ./pub/${app}${zone}

java -XX:+UseG1GC -Xmx200m -jar ${app}.jar --db=${db} --zone=${zone} --profile=test --script=../commands --groovy.bean=application-bean
