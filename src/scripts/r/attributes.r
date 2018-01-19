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
#1%   2%   3%   4%   5%   6%   7%   8%   9%  10%  11%  12%  13%  14%  15%  16%  17%  18% 
#  -29  -25  -23  -21  -20  -19  -18  -17  -16  -16  -15  -15  -14  -14  -13  -13  -12  -12 
#19%  20%  21%  22%  23%  24%  25%  26%  27%  28%  29%  30%  31%  32%  33%  34%  35%  36% 
#  -11  -11  -10  -10  -10  -10   -9   -9   -8   -8   -8   -7   -7   -7   -6   -6   -6   -5 
#37%  38%  39%  40%  41%  42%  43%  44%  45%  46%  47%  48%  49%  50%  51%  52%  53%  54% 
#  -5   -5   -5   -4   -4   -4   -3   -3   -3   -2   -2   -2   -1   -1   -1    0    0    0 
#55%  56%  57%  58%  59%  60%  61%  62%  63%  64%  65%  66%  67%  68%  69%  70%  71%  72% 
#  0    1    1    2    2    3    3    4    4    5    5    6    6    7    8    8    9   10 
#73%  74%  75%  76%  77%  78%  79%  80%  81%  82%  83%  84%  85%  86%  87%  88%  89%  90% 
#  11   11   12   13   14   15   17   18   20   21   23   25   27   30   33   36   40   45 
#91%  92%  93%  94%  95%  96%  97%  98%  99% 
#  51   57   66   76   91  113  152 1440 1440 

# => use 3 hours =180 min for cancelled/delayed
filtered <- dtrain[dtrain$arrdelay<24*60,]
summary(filtered)
quantile(
  x=filtered$arrdelay,
  probs=seq(from=0.01,to=0.99,by=0.01))
#-----------------------------------------------------------------
dev.on(
  file=plot.file(
    dataset='ontime',
    problem='l2',
    prefix='arrdelay'),
  aspect=0.5,
  width=1280)
ggplot(data=filtered, aes(filtered$arrdelay)) + 
  geom_histogram(binwidth=1)
dev.off()
#-----------------------------------------------------------------
