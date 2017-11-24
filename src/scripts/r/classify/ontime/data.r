# wahpenayo at gmail dot com
# 2017-11-23
# after https://github.com/szilard/benchm-ml/blob/master/0-init/2-gendata.txt
# Note: 
# * Replacing DepTime (actual departure time => target leak) 
# by CRSDepTIme (scheduled departure time).
# * Replacing dep_delayed_15min by arr_delayed_15min,
# because customers care about arrival delay, not departure delay.
# * Treated a cancelled or diverted flight as delayed, rather than
# missing.
# * Adding other obvious predictors, because ... why not?
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
#source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
set.seed(123)

# train on 2005 + 2006, test and validate on 2007
# should probably be training on 2-3 years, next year test, year
# after that validate

# requires inflating the bz2 files first
#d1a <- fread('2005.csv')
#d1b <- fread('2006.csv')
#d2 <- fread('2007.csv')
# very slow, but no 700gb temp files, and only do it once
d1a <- read.csv('data/ontime/2005.csv.bz2')
d1b <- read.csv('data/ontime/2006.csv.bz2')
d2 <- read.csv('data/ontime/2007.csv.bz2')

d1 <- rbind(d1a, d1b)

#-----------------------------------------------------------------
delayed <- function (data) {
  ((data$Cancelled == 1) 
  | (data$Diverted == 1) 
  | (data$ArrDelay>=15))
}
#-----------------------------------------------------------------
d1$arr_delayed_15min <- ifelse(delayed(d1),'Y','N') 
d2$arr_delayed_15min <- ifelse(delayed(d2),'Y','N') 

# handling cancelled and diverted flights takes care of missing
# delay values
#d1 <- d1[!is.na(d1$ArrDelay),]
#d2 <- d2[!is.na(d2$ArrDelay),]

for (k in c('Month','DayofMonth','DayOfWeek')) {
  d1[,k] <- paste0('c-',as.character(d1[,k]))
  d2[,k] <- paste0('c-',as.character(d2[,k]))
}


# Note: szilard uses DepTime (unknowable actual departure time)
# as a predictor. I've replaced that by CRSDepTime (scheduled
# departure time), and added scheduled arrival time and 
# scheduled elapsed time.

cols <- c('Month', 'DayofMonth', 'DayOfWeek', 
  'CRSDepTime', 'CRSArrTime', 'CRSElapsedTime', 'Distance',
  'UniqueCarrier','Origin', 'Dest',
  'arr_delayed_15min')
d1 <- d1[, cols]
d2 <- d2[, cols]

data.folder <- file.path('data','classify','ontime')
dir.create(
  path=data.folder,
  showWarnings=FALSE,
  recursive=TRUE)

# changed from original to write gzipped directly
for (n in c(1e4,1e5,1e6,1e7)) {
  f <- gzfile(
    file.path(data.folder,
      paste0('train-',n/1e6,'m.csv.gz')),'w')
  write.table(
    d1[sample(nrow(d1),n),], 
    file = f, 
    row.names = FALSE, 
    sep = ',',
    quote=FALSE)
  close(f)
}
idx_test <- sample(nrow(d2),1e5)
idx_valid <- sample(setdiff(1:nrow(d2),idx_test),1e5)
testf <- gzfile(file.path(data.folder,'test.csv.gz'),'w')
write.table(
  d2[idx_test,], 
  file = testf, 
  row.names = FALSE, 
  sep = ',',
  quote=FALSE)
close(testf)
validf <- gzfile(file.path(data.folder,'valid.csv.gz'),'w')
write.table(
  d2[idx_valid,], 
  file = validf, 
  row.names = FALSE, 
  sep = ',',
  quote=FALSE)
close(validf)
#-------------------------------------------------------------------------------
