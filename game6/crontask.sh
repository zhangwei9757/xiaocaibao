#! /bin/bash                                                                       
cd /home/ubuntu                                                                    
                                                                                   
# ÿ��Сʱ��55�ֻ���������ű�                                                     
                                                                                   
day=`date +%Y-%m-%d`                                                               
                                                                                   
ts=`date +%H:%M`                                                                   
                                                                                   
# 28������7���6������������                                                      
if [[ "$day" = "2017-10-28" ]]                                                     
then                                                                               
    if [[ "$ts" > "06:00" ]] && [[ "$ts" < "07:00" ]]                                                                                                                                            
    then                                                                           
        echo "`date` -- 28��7����������" >> 1log                                          
        supervisorctl status >> 1log                                               
        sleep 1s                                                                   
        supervisorctl reread                                                       
        sleep 1s                                                                   
        supervisorctl update                                                       
        sleep 1s                                                                   
        supervisorctl start x6                                                     
        sleep 30s                                                                  
        echo "`date` -- ���������״̬����" >> 1log                                          
        supervisorctl status >> 1log                                               
    fi                                                                             
fi                                                                                 
                                                                                                                                                                                                                                                                              
                                                                                   
                                                                                   
#echo "��ʱ�ű���ʼ����: `date "+%Y-%m-%d"`" >> 1log                               
#echo "״̬:" >> 1log                                                              
#supervisorctl status >> 1log                                                      
#sleep 2s                                                                          
# ����ҹ�������������ʱ����֮��� 