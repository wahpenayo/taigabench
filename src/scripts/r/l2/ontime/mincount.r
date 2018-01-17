# wahpenayo at gmail dot com
# 2018-01-16
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
readr.show_progress <- FALSE
#-----------------------------------------------------------------
dataset <- 'ontime'
problem <- 'l2'
response <- 'arrdelay'
dataf <- ontime.data
dtest <- dataf(test.file(dataset=dataset))
#suffixes <- c('32768','131072','524288','2097152','8388608','33554432')
suffix <- '131072'
mincounts <- c(15,31,63,127,255,511)
#-----------------------------------------------------------------
sweep.mincount(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  suffix=suffix,
  trainf=l2.randomForestSRC,
  prefix='randomForestSRC',
  mincounts=mincounts)
#-----------------------------------------------------------------
