app=guild
zone=1

mkdir -p ./pub/${app}${zone}
cp ${app}/target/${app}-1.0.jar ./pub/${app}${zone}/${app}.jar
cd ./pub/${app}${zone}
java -XX:+UseG1GC -Xmx300m -Xms32m -jar ${app}.jar --zone=${zone} --profile=007 --script=../commands
