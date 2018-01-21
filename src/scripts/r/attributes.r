# wahpenayo at gmail dot com
# 2018-01-18
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
suffix <- '8388608'
trainfile <- train.file(dataset=dataset,suffix=suffix)
dtrain <- dataf(trainfile)
#-----------------------------------------------------------------
summary(dtrain)
quantile(
  x=dtrain$arrdelay,
  probs=seq(from=0.01,to=0.99,by=0.01))
#-----------------------------------------------------------------
dev.on(
  file=plot.file(
    dataset='ontime',
    problem='l2',
    prefix='arrdelay'),
  aspect=0.5,
  width=1280)
ggplot(data=dtrain, aes(dtrain$arrdelay)) + 
  geom_histogram(bins=1000) +
  # scale_y_log10() +
  scale_x_continuous(breaks = scales::pretty_breaks(n = 20))
dev.off()
quantile(
  x=dtrain$arrdelay,
  probs=seq(from=0.90,to=0.999,by=0.001))
#-----------------------------------------------------------------
# => use 3 hours =180 min for cancelled/delayed
filtered <- dtrain[dtrain$arrdelay<4*60,]
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
