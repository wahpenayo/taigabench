# wahpenayo at gmail dot com
# 2017-11-16
# from https://github.com/szilard/benchm-ml/blob/master/0-init/2-gendata.txt
#-----------------------------------------------------------------
setwd('e:/porta/projects/taigabench/data/ontime')
#library(data.table)
set.seed(123)

# train on 2005 + 2006, test and validate on 2007
# should probably be training on 2-3 years, next year test, year
# after that validate

# requires inflating the bz2 files first
#d1a <- fread('2005.csv')
#d1b <- fread('2006.csv')
#d2 <- fread('2007.csv')
# very slow, but no 700gb temp files, and only do it once
d1a <- read.csv('2005.csv.bz2')
d1b <- read.csv('2006.csv.bz2')
d2 <- read.csv('2007.csv.bz2')

d1 <- rbind(d1a, d1b)

d1 <- d1[!is.na(d1$DepDelay),]
d2 <- d2[!is.na(d2$DepDelay),]

for (k in c('Month','DayofMonth','DayOfWeek')) {
  d1[,k] <- paste0('c-',as.character(d1[,k]))
  d2[,k] <- paste0('c-',as.character(d2[,k]))
}

d1$dep_delayed_15min <- ifelse(d1$DepDelay>=15,'Y','N') 
d2$dep_delayed_15min <- ifelse(d2$DepDelay>=15,'Y','N') 

cols <- c('Month', 'DayofMonth', 'DayOfWeek', 'DepTime', 
  'UniqueCarrier','Origin', 'Dest', 'Distance',
  'dep_delayed_15min')
d1 <- d1[, cols]
d2 <- d2[, cols]

# changed from original to write gzipped directly
for (n in c(1e4,1e5,1e6,1e7)) {
  f <- gzfile(
    file.path('classify',paste0('train-',n/1e6,'m.csv.gz')),'w')
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
testf <- gzfile(file.path('classify','test.csv.gz'),'w')
write.table(
  d2[idx_test,], 
  file = testf, 
  row.names = FALSE, 
  sep = ',',
  quote=FALSE)
close(testf)
validf <- gzfile(file.path('classify','valid.csv.gz'),'w')
write.table(
  d2[idx_valid,], 
  file = validf, 
  row.names = FALSE, 
  sep = ',',
  quote=FALSE)
close(validf)
#-------------------------------------------------------------------------------