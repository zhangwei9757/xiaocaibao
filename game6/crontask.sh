#! /bin/bash                                                                       
cd /home/ubuntu                                                                    
                                                                                   
# 每个小时的55分会运行这个脚本                                                     
                                                                                   
day=`date +%Y-%m-%d`                                                               
                                                                                   
ts=`date +%H:%M`                                                                   
                                                                                   
# 28号早上7点把6区服务器启动                                                      
if [[ "$day" = "2017-10-28" ]]                                                     
then                                                                               
    if [[ "$ts" > "06:00" ]] && [[ "$ts" < "07:00" ]]                                                                                                                                            
    then                                                                           
        echo "`date` -- 28号7点启动服务" >> 1log                                          
        supervisorctl status >> 1log                                               
        sleep 1s                                                                   
        supervisorctl reread                                                       
        sleep 1s                                                                   
        supervisorctl update                                                       
        sleep 1s                                                                   
        supervisorctl start x6                                                     
        sleep 30s                                                                  
        echo "`date` -- 完成启动，状态如下" >> 1log                                          
        supervisorctl status >> 1log                                               
    fi                                                                             
fi                                                                                 
                                                                                                                                                                                                                                                                              
                                                                                   
                                                                                   
#echo "定时脚本开始运行: `date "+%Y-%m-%d"`" >> 1log                               
#echo "状态:" >> 1log                                                              
#supervisorctl status >> 1log                                                      
#sleep 2s                                                                          
# 可以夜间进行重启，定时启动之类的 