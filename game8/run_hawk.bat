# 中心服务器
app=hawk
zone=1

mkdir -p ./pub/${app}${zone}

cp ${app}/target/${app}-1.0.jar ./pub/${app}${zone}/${app}.jar

cd ./pub/${app}${zone}
java -XX:+UseG1GC -Xmx300m -jar ${app}.jar --zone=${zone} --profile=test --script=../commands
