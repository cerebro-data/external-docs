Search.setIndex({docnames:["index","modules","okera","okera.tests"],envversion:{"sphinx.domains.c":1,"sphinx.domains.changeset":1,"sphinx.domains.cpp":1,"sphinx.domains.javascript":1,"sphinx.domains.math":2,"sphinx.domains.python":1,"sphinx.domains.rst":1,"sphinx.domains.std":1,"sphinx.ext.viewcode":1,sphinx:55},filenames:["index.rst","modules.rst","okera.rst","okera.tests.rst"],objects:{"":{okera:[2,0,0,"-"]},"okera.botocore_patch":{patch_botocore:[2,1,1,""]},"okera.concurrency":{BaseBackgroundTask:[2,2,1,""],ConcurrencyController:[2,2,1,""],TaskHandlerProcess:[2,2,1,""],default_max_client_process_count:[2,1,1,""]},"okera.concurrency.BaseBackgroundTask":{Name:[2,3,1,""],set_handler:[2,4,1,""]},"okera.concurrency.ConcurrencyController":{enqueueTask:[2,4,1,""],start:[2,4,1,""],stop:[2,4,1,""],terminate:[2,4,1,""]},"okera.concurrency.TaskHandlerProcess":{run:[2,4,1,""]},"okera.odas":{JsonScanTask:[2,2,1,""],OkeraContext:[2,2,1,""],OkeraFsStream:[2,2,1,""],PandasScanTask:[2,2,1,""],PlannerConnection:[2,2,1,""],ScanTask:[2,2,1,""],WorkerConnection:[2,2,1,""],context:[2,1,1,""],version:[2,1,1,""]},"okera.odas.JsonScanTask":{deserialize:[2,4,1,""]},"okera.odas.OkeraContext":{connect:[2,4,1,""],connect_worker:[2,4,1,""],disable_auth:[2,4,1,""],enable_kerberos:[2,4,1,""],enable_token_auth:[2,4,1,""],get_auth:[2,4,1,""],get_name:[2,4,1,""],get_retries:[2,4,1,""],get_timezone:[2,4,1,""],get_token:[2,4,1,""]},"okera.odas.OkeraFsStream":{read:[2,4,1,""]},"okera.odas.PandasScanTask":{deserialize:[2,4,1,""]},"okera.odas.PlannerConnection":{cat:[2,4,1,""],close:[2,4,1,""],execute_ddl:[2,4,1,""],execute_ddl_table_output:[2,4,1,""],get_catalog_objects_at:[2,4,1,""],get_protocol_version:[2,4,1,""],list_databases:[2,4,1,""],list_dataset_names:[2,4,1,""],list_datasets:[2,4,1,""],ls:[2,4,1,""],open:[2,4,1,""],plan:[2,4,1,""],scan_as_json:[2,4,1,""],scan_as_pandas:[2,4,1,""],set_application:[2,4,1,""]},"okera.odas.ScanTask":{deserialize:[2,4,1,""]},"okera.odas.WorkerConnection":{close:[2,4,1,""],close_task:[2,4,1,""],exec_task:[2,4,1,""],fetch:[2,4,1,""],get_protocol_version:[2,4,1,""],set_application:[2,4,1,""]},"okera.tests":{pycerebro_test_common:[3,0,0,"-"],test_basic:[3,0,0,"-"],test_connections:[3,0,0,"-"],test_fs:[3,0,0,"-"],test_nightly:[3,0,0,"-"],test_scans:[3,0,0,"-"]},"okera.tests.pycerebro_test_common":{configure_botocore_patch:[3,1,1,""],get_env_var:[3,1,1,""],get_planner:[3,1,1,""],get_test_context:[3,1,1,""],get_worker:[3,1,1,""],identity:[3,1,1,""]},"okera.tests.test_basic":{BasicTest:[3,2,1,""]},"okera.tests.test_basic.BasicTest":{test_all_types_empty:[3,4,1,""],test_all_types_empty_json:[3,4,1,""],test_all_types_json:[3,4,1,""],test_all_types_null_json:[3,4,1,""],test_all_types_null_pandas:[3,4,1,""],test_all_types_pandas:[3,4,1,""],test_basic:[3,4,1,""],test_binary_data:[3,4,1,""],test_catalog:[3,4,1,""],test_column_order_scan_as_pandas:[3,4,1,""],test_connection:[3,4,1,""],test_ddl:[3,4,1,""],test_filter_with_empty_rows_pandas:[3,4,1,""],test_impersonation:[3,4,1,""],test_pick_host:[3,4,1,""],test_plan:[3,4,1,""],test_planner:[3,4,1,""],test_random_host:[3,4,1,""],test_requesting_user:[3,4,1,""],test_version:[3,4,1,""],test_warnings:[3,4,1,""],test_worker:[3,4,1,""]},"okera.tests.test_connections":{ConnectionErrorsTest:[3,2,1,""]},"okera.tests.test_connections.ConnectionErrorsTest":{test_kerberos_server_no_override:[3,4,1,""],test_kerberos_server_valid_client:[3,4,1,""],test_token_server_no_token:[3,4,1,""],test_token_server_valid_token:[3,4,1,""],test_token_server_wrong_token:[3,4,1,""],test_unauthenticated_server_planner:[3,4,1,""],test_unauthenticated_server_worker:[3,4,1,""]},"okera.tests.test_fs":{FsTest:[3,2,1,""],RegisteredTest:[3,2,1,""]},"okera.tests.test_fs.FsTest":{test_as_testuser:[3,4,1,""],test_cat:[3,4,1,""],test_errors:[3,4,1,""],test_ls:[3,4,1,""]},"okera.tests.test_fs.RegisteredTest":{test_as_testuser:[3,4,1,""],test_basic:[3,4,1,""],test_dropping:[3,4,1,""],test_masking:[3,4,1,""]},"okera.tests.test_nightly":{ConnectionErrorsTest:[3,2,1,""]},"okera.tests.test_nightly.ConnectionErrorsTest":{test_okera_fs_read:[3,4,1,""],test_okera_sample_users:[3,4,1,""]},"okera.tests.test_scans":{BasicTest:[3,2,1,""]},"okera.tests.test_scans.BasicTest":{test_duplicate_cols:[3,4,1,""],test_large_decimals:[3,4,1,""],test_nulls:[3,4,1,""],test_scan_as_json_max_records:[3,4,1,""],test_scan_as_pandas_max_records:[3,4,1,""],test_sparse_data:[3,4,1,""],test_timestamp_functions:[3,4,1,""]},okera:{botocore_patch:[2,0,0,"-"],check_and_patch_botocore:[2,1,1,""],concurrency:[2,0,0,"-"],get_default_context:[2,1,1,""],initialize_default_context:[2,1,1,""],odas:[2,0,0,"-"],should_patch_botocore:[2,1,1,""],tests:[3,0,0,"-"]}},objnames:{"0":["py","module","Python module"],"1":["py","function","Python function"],"2":["py","class","Python class"],"3":["py","attribute","Python attribute"],"4":["py","method","Python method"]},objtypes:{"0":"py:module","1":"py:function","2":"py:class","3":"py:attribute","4":"py:method"},terms:{"abstract":2,"byte":2,"case":3,"class":[2,3],"default":[2,3],"import":2,"int":2,"return":2,"true":2,DNS:2,The:2,Used:2,amt:2,ani:2,api:2,applic:2,application_nam:2,arg:2,auth:2,authent:2,back:2,backward:2,base:[2,3],basebackgroundtask:2,basictest:3,batch:2,begin:2,behav:2,being:2,botocore_patch:[0,1],call:2,can:2,care:2,cat:2,cda:2,check_and_patch_botocor:2,close:2,close_task:2,coercer:3,columnar_record:2,compat:2,concurr:[0,1],concurrency_ctl:2,concurrencycontrol:2,config:2,configur:2,configure_botocore_patch:3,conn:2,connect:2,connect_work:2,connectionerrorstest:3,consist:2,contain:2,content:[0,1],context:2,ctx:2,data:2,datafram:2,dataset:2,default_max_client_process_count:2,definit:2,delimit:2,descript:2,deseri:2,diagnost:2,dictionari:2,directli:2,disabl:2,disable_auth:2,doctest:2,done:2,ellipsi:2,enabl:2,enable_kerbero:2,enable_token_auth:2,enqueuetask:2,errors_queu:2,exampl:2,exec_task:2,execut:2,execute_ddl:2,execute_ddl_table_output:2,fals:2,fetch:2,file:2,first:2,format:2,from:2,fstest:3,gener:2,get:2,get_auth:2,get_catalog_objects_at:2,get_default_context:2,get_env_var:3,get_nam:2,get_plann:3,get_protocol_vers:2,get_retri:2,get_test_context:3,get_timezon:2,get_token:2,get_work:3,guarante:2,handl:2,handler:2,host:[2,3],host_overrid:2,hostnam:2,ident:3,ignore_error:2,index:0,initialize_default_context:2,intend:2,json:2,jsonscantask:2,kei:2,kerbero:2,kwarg:2,level:2,librari:2,like:2,line:2,list:2,list_databas:2,list_dataset:2,list_dataset_nam:2,localhost:2,log:2,max_client_process_count:2,max_record:2,max_task_count:2,maximum:2,mechan:2,method:2,methodnam:3,metrics_dict:2,modul:[0,1],most:2,multiprocess:2,must:2,name:[2,3],need:2,next:2,none:[2,3],note:2,num_record:2,number:2,obj:2,object:2,oda:[0,1],okera:0,okera_sampl:2,okeracontext:2,okerafsstream:2,open:2,option:2,output_queu:2,overridden:2,packag:[0,1],page:0,panda:2,pandasscantask:2,paramet:2,part:2,particular:2,patch_botocor:2,pick:2,plan:2,plan_host:2,planner:2,plannerconnect:2,popul:2,port:[2,3],portion:2,princip:2,print:2,process:2,purpos:2,pycerebro_test_common:[1,2],pyokera:2,quote_str:2,random:2,read:2,realm:2,record:2,registeredtest:3,request:2,requesting_us:2,requir:2,resolv:2,result:2,retri:2,rpc:2,run:2,runtest:3,sampl:2,scan:2,scan_as_json:2,scan_as_panda:2,scantask:2,schema:2,search:0,second:2,select:2,send:2,serial:2,server:2,servic:2,service_nam:2,session:2,set:2,set_appl:2,set_handl:2,should:2,should_patch_botocor:2,sourc:[2,3],specifi:2,sql:2,start:2,statement:2,stop:2,str:2,stream:2,string:2,strings_as_utf8:2,sub:2,submodul:[0,1],subpackag:[0,1],subsequ:2,task:2,task_queu:2,taskhandlerprocess:2,tbl:2,termin:2,test:[1,2],test_all_types_empti:3,test_all_types_empty_json:3,test_all_types_json:3,test_all_types_null_json:3,test_all_types_null_panda:3,test_all_types_panda:3,test_as_testus:3,test_bas:[1,2],test_binary_data:3,test_cat:3,test_catalog:3,test_column_order_scan_as_panda:3,test_connect:[1,2],test_ddl:3,test_drop:3,test_duplicate_col:3,test_error:3,test_f:[1,2],test_filter_with_empty_rows_panda:3,test_imperson:3,test_kerberos_server_no_overrid:3,test_kerberos_server_valid_cli:3,test_l:3,test_large_decim:3,test_mask:3,test_nightli:[1,2],test_nul:3,test_okera_fs_read:3,test_okera_sample_us:3,test_pick_host:3,test_plan:3,test_plann:3,test_random_host:3,test_requesting_us:3,test_scan:[1,2],test_scan_as_json_max_record:3,test_scan_as_pandas_max_record:3,test_sparse_data:3,test_timestamp_funct:3,test_token_server_no_token:3,test_token_server_valid_token:3,test_token_server_wrong_token:3,test_unauthenticated_server_plann:3,test_unauthenticated_server_work:3,test_vers:3,test_warn:3,test_work:3,testcas:3,thi:2,thrift_servic:2,timeout:2,token:2,token_fil:2,token_str:2,top:2,two:2,type:2,typic:2,unittest:3,unlimit:2,urllib:2,use:2,user:2,utc:2,valu:2,version:2,warn:2,when:2,which:2,worker:2,worker_count:2,workerconnect:2,wrapper:2},titles:["Welcome to pyokera\u2019s documentation!","okera","okera package","okera.tests package"],titleterms:{botocore_patch:2,concurr:2,content:[2,3],document:0,indic:0,modul:[2,3],oda:2,okera:[1,2,3],packag:[2,3],pycerebro_test_common:3,pyokera:0,submodul:[2,3],subpackag:2,tabl:0,test:3,test_bas:3,test_connect:3,test_f:3,test_nightli:3,test_scan:3,welcom:0}})