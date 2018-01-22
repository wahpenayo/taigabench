# wahpenayo at gmail dot com
# 2018-01-21
#-----------------------------------------------------------------
# explore the data
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
dataset <- 'ontime'
dataf <- ontime.data
#suffixes <- c('32768','131072','524288','2097152','8388608','33554432')
suffix <- '8388608' # OOM with 33554432 records
trainfile <- train.file(dataset=dataset,suffix=suffix)
dtrain <- dataf(trainfile)
#-----------------------------------------------------------------
summary(dtrain)
#-----------------------------------------------------------------
for (col in colnames(dtrain)) {
  print(col)
  if (is.numeric(dtrain[[col]])) {
    hist.numeric(
      data=dtrain,col=col,dataset='ontime',problem='l2')
  } else {
    hist.factor(
      data=dtrain,col=col,dataset='ontime',problem='l2')
  }
}
#-----------------------------------------------------------------
quantile(
  x=dtrain$arrdelay,
  probs=seq(from=0.01,to=0.99,by=0.01))
quantile(
  x=dtrain$arrdelay,
  probs=seq(from=0.90,to=0.999,by=0.001)) 
#-----------------------------------------------------------------
# => use 3 hours =180 min for cancelled/delayed
filtered <- dtrain[dtrain$arrdelay<2*60,]
summary(filtered)
quantile(
  x=filtered$arrdelay,
  probs=seq(from=0.90,to=0.999,by=0.001))
dev.on(
  file=plot.file(
    dataset='ontime',
    problem='l2',
    prefix='arrdelay-filtered'),
  aspect=0.5,
  width=1280)
ggplot(data=filtered, aes(filtered$arrdelay)) + 
  geom_histogram(bins=1000) +
  # scale_y_log10() +
  scale_x_continuous(breaks = scales::pretty_breaks(n = 20))
dev.off()
#-----------------------------------------------------------------
