# wahpenayo at gmail dot com
# since 2016-11-11
# 2017-11-27

import time
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics

def get_dummies(d, col):
    dd = pd.get_dummies(d.ix[:, col])
    dd.columns = [col + "_%s" % c for c in dd.columns]
    return(dd)

vars_categ = ["cmonth","cdayofmonth","cdayofweek",\
              "uniquecarrier", "origin", "dest"]
vars_num = ["month","dayofmonth","dayofweek","dayofyear",\
            "daysaftermar1",\
            "crsdeptime","crsarrtime","crselapsedtime","distance"]

results = []

#for suffix in ["0.01", "0.1", "1", "10"] :  
#for suffix in ["10"] :  
# fails with 33554432 records in 64gb host
#for suffix in ["8192","65536","524288","4194304","33554432"] :  
for suffix in ["8192","65536","524288","4194304"] :
    start = time.clock()
    d_train = \
    pd.read_csv("data/ontime/" + "train-" + suffix + ".csv.gz")
    d_test = pd.read_csv("data/ontime/test.csv.gz")
    d_train_test = d_train.append(d_test)
    X_train_test_categ = \
    pd.concat([get_dummies(d_train_test, col) for col in vars_categ], \
              axis = 1)
    X_train_test = \
    pd.concat([X_train_test_categ, d_train_test.ix[:,vars_num]], \
              axis = 1)
    y_train_test = np.where(d_train_test["arrdelay"]>=15, 1, 0)
    X_train = X_train_test[0:d_train.shape[0]]
    y_train = y_train_test[0:d_train.shape[0]]
    X_test = X_train_test[d_train.shape[0]:]
    y_test = y_train_test[d_train.shape[0]:]
    datatime = time.clock() - start
    
    start = time.clock()
    md = RandomForestClassifier(n_estimators = 255,\
                                n_jobs = -1,\
                                max_depth=1024,\
                                min_samples_leaf=17)
    md.fit(X_train, y_train)
    traintime = time.clock() - start
    
    start = time.clock()
    phat = md.predict_proba(X_test)[:,1]
    predicttime = time.clock() - start
    
    metrics.roc_auc_score(y_test, phat)
    
    start = time.clock()
    auc = metrics.roc_auc_score(y_test, phat)
    auctime = time.clock() - start
    results.append({'model' : 'scikit-learn', \
                    'ntrain' : d_train.shape[0], \
                    'ntest' : d_test.shape[0], \
                    'datatime' : datatime, \
                    'traintime' : traintime, \
                    'predicttime' : predicttime, \
                    'auctime' : auctime, \
                    'auc' : auc, })
    df = pd.DataFrame(data=results)
    df.to_csv('output/classify/ontime/scikit-learn.results.csv', \
              index=False)

