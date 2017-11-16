# John Alan McDonald 2017-01-31
#-------------------------------------------------------------------------------
library(data.table)
library(readr)
library(ROCR)
library(randomForest)
library(xgboost)
library(parallel)
library(Matrix)
library(h2o)
library(grid)
library(hexbin)
library(lattice)
library(latticeExtra)
library(ggplot2)
library(RColorBrewer)
#-------------------------------------------------------------------------------
my.theme <- function () {
 n <- 7
 text.color <- 'black'
 dark <- brewer.pal(n,'Dark2')
 pastel <- brewer.pal(n,'Pastel2')
 list(
   background = list(col="transparent"),
   #fontsize='16',
   axis.line=list(col='gray'),
   axis.text=list(col=text.color,cex=4),
   box.rectangle = list(col="darkgreen"),
   box.umbrella = list(col="darkgreen"),
   dot.line = list(col="#e8e8e8"),
   dot.symbol = list(col="darkgreen"),
   par.xlab.text=list(col=text.color,cex=4),
   par.xlab.text=list(col=text.color,cex=4),
   par.ylab.text=list(col=text.color,cex=1.5),
   par.main.text=list(col=text.color,cex=2),
   par.sub.text=list(col=text.color,cex=1.5),
   plot.line = list(col="darkgreen"),
   plot.symbol = list(col="darkgreen"),
   plot.polygon = list(col="darkgreen"),
   reference.line = list(col="#e8e8e8"),
   regions = list(col = colorRampPalette(rev(brewer.pal(11,'RdYlGn')))(100)),
   shade.colors = list(palette=colorRampPalette(rev(brewer.pal(11,'RdYlGn')))),
   strip.border = list(col='gray'),
   strip.shingle = list(col=dark),
   strip.background = list(col='gray'),
   superpose.line = list(col = dark,lty = 1:n,lwd=1),
   superpose.polygon = list(col=dark,border=rep('#DDDDDD44',n),alpha=rep(0.3,n)),
   superpose.symbol = list(pch=c(1,3,6,0,5,16,17),cex=rep(1, n),col=dark,fontface='bold')) }
#lattice.options(default.theme = my.theme)
#lattice.options(lattice.theme = my.theme)
#-------------------------------------------------------------------------------
#theme_set(theme_bw())
#-------------------------------------------------------------------------------
# Open a png graphics device.
# aspect ratio is height/width

dev.on <- function (filename,aspect=(1050/1400),width=1400,theme=my.theme) {
 
 # make sure the folder is there
 dir.create(dirname(filename),showWarnings=FALSE,recursive=TRUE)
 
 # often the graphics device is stuck from the last failed run
 options(show.error.messages = FALSE)
 options(warn = -1)
 try( dev.off() )
 options(warn = 0)
 options(show.error.messages = TRUE)
 
 w <- width
 h <- aspect*w
 
 # The png device doesn't work under my R installation on linux.
 # The bitmap device doesn't work on my windowslaptop.
 # At least they both produce png files.
 plotF <- paste(filename,'png',sep='.')
 print(plotF)
 if ('windows'==.Platform$OS.type) {
  trellis.device(device='png',theme=theme,
    filename=plotF,width=w,height=h) }
 else { 
  trellis.device(device='bitmap',theme=theme,
    file=plotF,width=w,height=h,theme=theme) } }
#-------------------------------------------------------------------------------

write.tsv <- function (data, file) {
 write.table(x=data, quote=FALSE, sep='\t', row.names=FALSE, file=file) }

read.tsv <- function (file) {
 read.table(sep='\t', file=file) }

#-------------------------------------------------------------------------------
data.folder <- file.path('data','ontime')

train.file <- function (suffix='0.1m') {
 file.path(data.folder,paste("train-",suffix,".csv",sep='')) }

test.file <- function () {
 file.path(data.folder,paste("test.csv",sep='')) }

output.folder <- file.path('output','ontime')

predicted.file <- function (prefix) {
 gzfile(
   file.path(output.folder, paste(prefix,"pred.tsv.gz",sep='.'))) }

results.file <- function (prefix='all') {
 file.path(output.folder,paste(paste(prefix,"results.csv",sep='.'))) }

plot.file <- function (prefix) { file.path(output.folder,prefix) }
#-------------------------------------------------------------------------------
models <- c(
  'h2o',
  'randomForest',
  'randomForestSRC',
  'scikit-learn',
  'taiga',
  'xgboost')
model.colors <- c(
  '#386cb050',
  '#1b9e7750',
  '#66a61e50',
  '#a6761d50',
  '#e41a1cFF',
  '#75707050')
#-------------------------------------------------------------------------------
xgboost_randomForest <- function (suffix='0.01m') {
 
 set.seed(169544)
 
 start <- proc.time()
 
 d_train <- read_csv(train.file(suffix))
 d_test <- read_csv(test.file())
 
 X_train_test <- sparse.model.matrix(dep_delayed_15min ~ .-1, data = rbind(d_train, d_test))
 X_train <- X_train_test[1:nrow(d_train),]
 X_test <- X_train_test[(nrow(d_train)+1):(nrow(d_train)+nrow(d_test)),]
 #dim(X_train)
 
 datatime <- proc.time() - start
 
 # random forest with xgboost
 start <- proc.time()
 n_proc <- detectCores()
 md <- xgboost(
   data = X_train, 
   label = ifelse(d_train$dep_delayed_15min=='Y',1,0),
   nthread = n_proc, 
   nround = 1, 
   max_depth = 20,
   min_child_weight = 10,
   num_parallel_tree = 500, 
   subsample = 0.632,
   colsample_bytree = 1.0 / sqrt(length(X_train@x)/nrow(X_train)))
 traintime <- proc.time() - start
 
 start <- proc.time()
 phat <- predict(md, newdata = X_test)
 truth <- d_test[,"dep_delayed_15min"]
 truth <- as.numeric(ifelse(truth$dep_delayed_15min=='Y',1,0))
 prtr <- data.frame(prediction=phat,truth=as.numeric(truth))
 predicttime <- proc.time() - start
 
 write.tsv(data=prtr, file=predicted.file(paste('xgboost',suffix,sep='-')))
 
 start <- proc.time()
 rocr_pred <- prediction(phat, d_test$dep_delayed_15min)
 auc <- performance(rocr_pred, "auc")
 auctime <- proc.time() - start
 
 list(
   model='xgboost',
   ntrain=nrow(d_train),
   ntest=nrow(d_test),
   datatime=datatime['elapsed'],
   traintime=traintime['elapsed'],
   predicttime=predicttime['elapsed'],
   auctime=auctime['elapsed'],
   auc=auc@y.values[[1]])
}
#-------------------------------------------------------------------------------
h2o_randomForest <- function (suffix='0.01m') {
 
 set.seed(740189)
 h2o.init(max_mem_size="26g", nthreads=-1)

 start <- proc.time()
 dx_train <- h2o.importFile(path = train.file(suffix))
 dx_test <- h2o.importFile(path = test.file())
 Xnames <- names(dx_train)[which(names(dx_train)!="dep_delayed_15min")]
 datatime <- proc.time() - start
 
 start <- proc.time()
 md <- h2o.randomForest(
   x = Xnames, 
   y = "dep_delayed_15min", 
   training_frame = dx_train, 
   ntrees = 500,
   min_rows = 10,
   max_depth = 20)
 traintime <- proc.time() - start
 
 #show(md)
 
 start <- proc.time()
 prediction <- as.data.frame(predict(md,dx_test))
 truth <- as.data.frame(dx_test[,"dep_delayed_15min"])
 truth <- ifelse(truth$dep_delayed_15min=='Y',1,0)
 prtr <- data.frame(prediction=prediction$Y,truth=as.numeric(truth))
 summary(prtr)
 predicttime <- proc.time() - start
  
 write.tsv(data=prtr,file=predicted.file(paste('h2o',suffix,sep='-')))
 
 start <- proc.time()
 perf <- h2o.performance(md, dx_test)
 #show(perf)
 auc <- h2o.auc(perf)
 auctime <- proc.time() - start
 
 list(
   model='h2o',
   ntrain=nrow(dx_train),
   ntest=nrow(dx_test),
   datatime=datatime['elapsed'],
   traintime=traintime['elapsed'],
   predicttime=predicttime['elapsed'],
   auctime=auctime['elapsed'],
   auc=auc)
 
}
#-------------------------------------------------------------------------------
parallel_randomForest <- function (suffix='0.01m') {
 set.seed(1244985)
 
 start <- proc.time()
 d_train <- as.data.frame(fread(train.file(suffix)))
 d_test <- as.data.frame(fread(test.file()))
 datatime <- proc.time() - start
 
 #summary(d_train)
 #summary(d_test)
 ## "Can not handle categorical predictors with more than 53 categories."
 ## so need dummy variables/1-hot encoding
 ## - but then RF does not treat them as 1 variable
 
 X_train_test <-  model.matrix(dep_delayed_15min ~ ., data = rbind(d_train, d_test))
 X_train <- X_train_test[1:nrow(d_train),]
 X_test <- X_train_test[(nrow(d_train)+1):(nrow(d_train)+nrow(d_test)),]
 
 
 dim(X_train)
 
 # 'mc.cores' > 1 is not supported on Windows
 start <- proc.time()
 n_proc <- detectCores()
 mds <- mclapply(1:n_proc,
   function(x) randomForest(X_train, as.factor(d_train$dep_delayed_15min), 
      ntree = floor(500/n_proc)), mc.cores = n_proc)
 md <- do.call("combine", mds)
 traintime <- proc.time() - start   
 
 start <- proc.time()
 phat <- predict(md, newdata = d_test, type = "prob")[,"Y"]
 predicttime <- proc.time() - start   
 
 write.tsv(
   data=data.frame(prediction=phat,truth=ifelse(d_train$dep_delayed_15min=='Y',1,0)),
   file=predicted.file(paste('parallel.randomForest',suffix,sep='-')))
 
 start <- proc.time()
 rocr_pred <- prediction(phat, d_test$dep_delayed_15min)
 auc <- performance(rocr_pred, "auc")
 print(auc)
 auctime <- proc.time() - start
 
 #gc()
 #sapply(ls(),function(x) object.size(get(x))/1e6)
 list(
   model='parallel_randomForest',
   ntrain=nrow(d_train),
   ntest=nrow(d_test),
   datatime=datatime['elapsed'],
   traintime=traintime['elapsed'],
   predicttime=predicttime['elapsed'],
   auctime=auctime['elapsed'],
   auc=auc@y.values[[1]])
}
#-------------------------------------------------------------------------------
single_randomForest <- function (suffix='0.01m') {
 set.seed(1244985)
 
 start <- proc.time()
 
 d_train <- as.data.frame(fread(train.file(suffix)))
 d_test <- as.data.frame(fread(test.file()))
 
 X_train_test <-  model.matrix(dep_delayed_15min ~ ., data = rbind(d_train, d_test))
 X_train <- X_train_test[1:nrow(d_train),]
 X_test <- X_train_test[(nrow(d_train)+1):(nrow(d_train)+nrow(d_test)),]
 
 datatime <- proc.time() - start
 
 start <- proc.time()
 forest <- randomForest(x=X_train,
                        y=as.factor(d_train$dep_delayed_15min), 
                        ntree = 500, nodesize=10)
 traintime <- proc.time() - start   
 
 start <- proc.time()
 phat <- predict(forest, newdata = X_test, type = "prob")[,"Y"]
 predicttime <- proc.time() - start   
 
 write.tsv(
   data=data.frame(prediction=phat,truth=ifelse(d_test$dep_delayed_15min=='Y',1,0)),
   file=predicted.file(paste('single.randomForest',suffix,sep='-')))
 
 start <- proc.time()
 rocr_pred <- prediction(phat, d_test$dep_delayed_15min)
 auc <- performance(rocr_pred, "auc")
 print(auc)
 auctime <- proc.time() - start
 
 #gc()
 #sapply(ls(),function(x) object.size(get(x))/1e6)
 list(
   model='randomForest',
   ntrain=nrow(d_train),
   ntest=nrow(d_test),
   datatime=datatime['elapsed'],
   traintime=traintime['elapsed'],
   predicttime=predicttime['elapsed'],
   auctime=auctime['elapsed'],
   auc=auc@y.values[[1]])
}

