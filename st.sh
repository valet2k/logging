#!/bin/sh
java -jar db-derby-10.12.1.1-lib/lib/derbyrun.jar ij << EOF
connect 'jdbc:derby://localhost:1527/sampledb;create=true';
select * from t;
EOF
