# wahpenayo at gmail dot com
# 2017-11-29
#-----------------------------------------------------------------
# Load the necessary add-on packages, downloading and installing
# (in the user's R_LIBS_USER folder) if necessary.
load.packages <- function () {
  user.libs <- Sys.getenv('R_LIBS_USER')
  dir.create(user.libs,showWarnings=FALSE,recursive=TRUE)
  repos <- c('http://cran.fhcrc.org')
  packages <- 
    c('versions',
      'RCurl',
      'data.table', 
      'readr',
      'ROCR', 
      'randomForest',
      'randomForestSRC',
      'xgboost',
      'parallel',
      'Matrix', 
      #'h2o',
      'grid',
      #' 'hexbin',
      # lattice needed for the trellis device, could probably
      # rewrite dev.on to eliminate that
      'lattice',  
      'latticeExtra',
      'ggplot2',
      #'quantregForest',
      #'randomSurvivalForest',
      'RColorBrewer')
  for (package in packages) {
    found <- eval(call('require',package,quietly=TRUE))
    if (! found) { 
      install.packages(c(package),user.libs,repos=repos)
      eval(call('library',package)) } } }
#-----------------------------------------------------------------
load.packages()
# latest version of H2O directly from H2O
# first remove old version if necessary
# if ('package:h2o' %in% search()) { detach('package:h2o', unload=TRUE) }
# if ('h2o' %in% rownames(installed.packages())) { remove.packages('h2o') }
if (! eval(call('require','h2o',quietly=TRUE))) {
  install.packages(
    'h2o', 
    #Sys.getenv('R_LIBS_USER'),
    #type='source', 
    repos=(c('http://h2o-release.s3.amazonaws.com/h2o/latest_stable_R'))) 
}
#-----------------------------------------------------------------
# see
# https://stackoverflow.com/questions/27788968/how-would-one-check-the-system-memory-available-using-r-on-a-windows-machine
free.ram <- function () {
  if(Sys.info()[["sysname"]] == "Windows"){
    x <- system2("wmic", args =  "OS get FreePhysicalMemory /Value", stdout = TRUE)
    x <- x[grepl("FreePhysicalMemory", x)]
    x <- gsub("FreePhysicalMemory=", "", x, fixed = TRUE)
    x <- gsub("\r", "", x, fixed = TRUE)
    floor(as.integer(x) / (1024 * 1024))
  } else {
    stop("Only supported on Windows OS")
  }
}
#-----------------------------------------------------------------
# plots
#-----------------------------------------------------------------
my.theme <- function () {
  n <- 7
  text.color <- 'black'
  dark <- brewer.pal(n,'Dark2')
  pastel <- brewer.pal(n,'Pastel2')
  list(
    background=list(col='transparent'),
    #fontsize='16',
    axis.line=list(col='gray'),
    axis.text=list(col=text.color,cex=4),
    box.rectangle=list(col='darkgreen'),
    box.umbrella=list(col='darkgreen'),
    dot.line=list(col='#e8e8e8'),
    dot.symbol=list(col='darkgreen'),
    par.xlab.text=list(col=text.color,cex=4),
    par.xlab.text=list(col=text.color,cex=4),
    par.ylab.text=list(col=text.color,cex=1.5),
    par.main.text=list(col=text.color,cex=2),
    par.sub.text=list(col=text.color,cex=1.5),
    plot.line=list(col='darkgreen'),
    plot.symbol=list(col='darkgreen'),
    plot.polygon=list(col='darkgreen'),
    reference.line=list(col='#e8e8e8'),
    regions=list(col=colorRampPalette(rev(brewer.pal(11,'RdYlGn')))(100)),
    shade.colors=list(palette=colorRampPalette(rev(brewer.pal(11,'RdYlGn')))),
    strip.border=list(col='gray'),
    strip.shingle=list(col=dark),
    strip.background=list(col='gray'),
    superpose.line=list(col=dark,lty=1:n,lwd=1),
    superpose.polygon=list(col=dark,border=rep('#DDDDDD44',n),alpha=rep(0.3,n)),
    superpose.symbol=list(pch=c(1,3,6,0,5,16,17),cex=rep(1, n),col=dark,fontface='bold')) }
#lattice.options(default.theme=my.theme)
#lattice.options(lattice.theme=my.theme)
#-----------------------------------------------------------------
#theme_set(theme_bw())
#-----------------------------------------------------------------
# Open a png graphics device.
# aspect ratio is height/width

dev.on <- function (
  filename,
  aspect=(1050/1400),
  width=1400,
  theme=my.theme) {
  
  # make sure the folder is there
  dir.create(dirname(filename),showWarnings=FALSE,recursive=TRUE)
  
  # often the graphics device is stuck from the last failed run
  options(show.error.messages=FALSE)
  options(warn=-1)
  try( dev.off() )
  options(warn=0)
  options(show.error.messages=TRUE)
  
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
#-----------------------------------------------------------------
# consistent colors accross plots
models <- c(
  'h2o',
  'randomForest',
  'randomForestSRC',
  'scikit-learn',
  'taiga',
  'taiga-pfp',
  'taiga-mvp',
  'xgboost',
  'xgboost.exact')
model.colors <- c(
  '#386cb050',
  '#1b9e7750',
  '#66a61e50',
  '#a6761d50',
  '#e41a1cFF',
  '#e41a1cFF',
  '#1c1ae4FF',
  '#75707050',
  '#75707050')
#-----------------------------------------------------------------
#IO
#-----------------------------------------------------------------

write.tsv <- function (data, file) {
  write.table(
    x=data, 
    quote=FALSE, 
    sep='\t', 
    row.names=FALSE, 
    file=file) }

read.tsv <- function (file) {
  read.table(
    sep='\t', 
    file=file) }

#-----------------------------------------------------------------
# files
#-----------------------------------------------------------------
data.folder <- function (
  dataset=NULL, 
  problem=NULL) {
  if (is.null(problem)) {
    file.path('data',dataset)
  } else {
    file.path('data',problem,dataset)
  }
}

train.file <- function (
  dataset=NULL, 
  problem=NULL,
  suffix=NULL) {
  file.path(
    data.folder(problem=problem,dataset=dataset),
    paste('train-',suffix,'.csv.gz',sep='')) }

test.file <- function (
  dataset=NULL, 
  problem=NULL) {
  file.path(
    data.folder(problem=problem,dataset=dataset),
    paste('test.csv.gz',sep='')) }

output.folder <- function (
  dataset=NULL, 
  problem=NULL) {
  path <- file.path('output',problem,dataset)
  dir.create(
    path=path,
    showWarnings=FALSE,
    recursive=TRUE)
  path
}

predicted.file <- function (
  dataset=NULL, 
  problem=NULL,
  prefix) {
  gzfile(
    file.path(
      output.folder(problem=problem,dataset=dataset), 
      paste(prefix,'pred.tsv.gz',sep='.'))) }

results.file <- function (
  dataset=NULL, 
  problem=NULL,
  prefix='all') {
  file.path(
    output.folder(problem=problem,dataset=dataset),
    paste(paste(prefix,'results.csv',sep='.'))) }

plot.file <- function (
  dataset=NULL, 
  problem=NULL,
  prefix) { 
  file.path(
    output.folder(problem=problem,dataset=dataset),
    prefix) }
#-----------------------------------------------------------------
# h2o
#-----------------------------------------------------------------
# binary clasisfication only?
classify.h2o.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  maxmem=paste0(floor(0.9*free.ram()),'g'),
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.factor(dtrain[[response]]),
    is.factor(dtest[[response]]))
  
  # H2O requires Java 8 as of 2017-11-17, version 3.15.0.4103
  old.java.home <- Sys.getenv('JAVA_HOME')
  Sys.setenv(JAVA_HOME=Sys.getenv('JAVA8'))
  
  set.seed(740189)
  h2o.init(max_mem_size=maxmem, nthreads=-1)
  
  start <- proc.time()
  dx_train <- as.h2o(x=dtrain)
  dx_test <- as.h2o(x=dtest)
  Xnames <- 
    names(dx_train)[which(names(dx_train)!=response)]
  datatime <- proc.time() - start
  
  start <- proc.time()
  md <- h2o.randomForest(
    x=Xnames, 
    y=response, 
    training_frame=dx_train, 
    ntrees=ntrees,
    min_rows=mincount,
    max_depth=maxdepth)
  traintime <- proc.time() - start
  
  #show(md)
  
  start <- proc.time()
  pred <- as.data.frame(predict(md,dx_test))
  prtr <- data.frame(
    prediction=pred$Y,
    truth=ifelse(dtest[[response]]=='Y',1,0))
  predicttime <- proc.time() - start
  #summary(prtr)
  
  write.tsv(
    data=prtr,
    file=predicted.file(
      dataset=dataset,
      problem='classify',
      prefix=paste('h2o',suffix,sep='-')))
  
  start <- proc.time()
  perf <- h2o.performance(md, dx_test)
  #show(perf)
  auc <- h2o.auc(perf)
  auctime <- proc.time() - start
  
  results <- list(
    model='h2o',
    ntrain=nrow(dx_train),
    ntest=nrow(dx_test),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    auctime=auctime['elapsed'],
    auc=auc)
  Sys.setenv(JAVA_HOME=old.java.home)
  results
}
#-----------------------------------------------------------------
# l2 scalar regression
# differs from classification only in metric reported,.
l2.h2o.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  maxmem=paste0(floor(0.9*free.ram()),'g'),
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.numeric(dtrain[[response]]),
    is.numeric(dtest[[response]]))
  
  
  # H2O requires Java 8 as of 2017-11-17, version 3.15.0.4103
  old.java.home <- Sys.getenv('JAVA_HOME')
  Sys.setenv(JAVA_HOME=Sys.getenv('JAVA8'))
  
  set.seed(740189)
  h2o.init(max_mem_size=maxmem, nthreads=-1)
  
  start <- proc.time()
  dx_train <- as.h2o(x=dtrain)
  dx_test <- as.h2o(x=dtest)
  Xnames <- 
    names(dx_train)[which(names(dx_train)!=response)]
  datatime <- proc.time() - start
  
  start <- proc.time()
  md <- h2o.randomForest(
    x=Xnames, 
    y=response, 
    training_frame=dx_train, 
    ntrees=ntrees,
    min_rows=mincount,
    max_depth=maxdepth)
  traintime <- proc.time() - start
  
  #show(md)
  
  start <- proc.time()
  pred <- as.data.frame(predict(md,dx_test))
  prtr <- data.frame(
    prediction=pred$predict,
    truth=dtest[[response]])
  predicttime <- proc.time() - start
  #summary(prtr)
  
  write.tsv(
    data=prtr,
    file=predicted.file(
      dataset=dataset,
      problem='l2',
      prefix=paste('h2o',suffix,sep='-')))
  
  start <- proc.time()
  rmse <- sqrt(mean((prtr$truth-prtr$prediction)^2))
  rmsetime <- proc.time() - start
  
  results <- list(
    model='h2o',
    ntrain=nrow(dx_train),
    ntest=nrow(dx_test),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    rmsetime=rmsetime['elapsed'],
    rmse=rmse)
  Sys.setenv(JAVA_HOME=old.java.home)
  results
}
#-----------------------------------------------------------------
# xgboost
#-----------------------------------------------------------------
# save mode callback that does nothing
cb.save.model <- function(
  save_period=0, 
  save_name='xgboost.model') {
  
  if (save_period < 0)
    stop("'save_period' cannot be negative")
  
  callback <- function(env=parent.frame()) {
#    if (is.null(env$bst))
#      stop("'save_model' callback requires the 'bst' booster object in its calling frame")
#    
#    if ((save_period > 0 && (env$iteration - env$begin_iteration) %% save_period == 0) ||
#      (save_period == 0 && env$iteration == env$end_iteration))
#      xgb.save(env$bst, sprintf(save_name, env$iteration))
  }
  attr(callback, 'call') <- match.call()
  attr(callback, 'name') <- 'cb.save.model'
  callback
}
#-----------------------------------------------------------------
# binary classification only?
classify.xgboost.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.factor(dtrain[[response]]),
    is.factor(dtest[[response]]))
  
  set.seed(169544)
  
  start <- proc.time()
  
  X_train_test <- sparse.model.matrix(
    as.formula(paste(response, '~ .-1')), 
    data=rbind(dtrain, dtest))
  X_train <- X_train_test[1:nrow(dtrain),]
  X_test <- 
    X_train_test[(nrow(dtrain)+1):(nrow(dtrain)+nrow(dtest)),]
  
  datatime <- proc.time() - start
  
  modelfile <-file.path(
    output.folder(dataset=dataset,problem='classify'),
    'xgboost.model')
  
  start <- proc.time()
  n_proc <- detectCores()
  md <- xgboost(
    data=X_train, 
    label=ifelse(dtrain[,response]=='Y',1,0),
    nthread=n_proc, 
    nround=1, 
    max_depth=maxdepth,
    min_child_weight=mincount,
    num_parallel_tree=ntrees, 
    subsample=0.632,
    colsample_bytree=
      1.0 / sqrt(length(X_train@x)/nrow(X_train)),
    save_name=modelfile,
    callbacks=list(cb.save.model(save_name=modelfile)))
  traintime <- proc.time() - start
  
  start <- proc.time()
  phat <- predict(md, newdata=X_test)
  truth <- dtest[,response]
  truth <- as.numeric(ifelse(truth[,response]=='Y',1,0))
  prtr <- data.frame(prediction=phat,truth=as.numeric(truth))
  predicttime <- proc.time() - start
  
  write.tsv(
    data=prtr, 
    file=predicted.file(
      prefix=paste('xgboost.exact',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, dtest[,response])
  auc <- performance(rocr_pred, 'auc')
  auctime <- proc.time() - start
  
  list(
    model='xgboost',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    auctime=auctime['elapsed'],
    auc=auc@y.values[[1]])
}
#-----------------------------------------------------------------
# binary classification only?
classify.xgboost.exact.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.factor(dtrain[[response]]),
    is.factor(dtest[[response]]))
  
  set.seed(169544)
  
  start <- proc.time()
  
  X_train_test <- sparse.model.matrix(
    as.formula(paste(response, '~ .-1')), 
    data=rbind(dtrain, dtest))
  X_train <- X_train_test[1:nrow(dtrain),]
  X_test <- 
    X_train_test[(nrow(dtrain)+1):(nrow(dtrain)+nrow(dtest)),]
  
  datatime <- proc.time() - start
  
  modelfile <-file.path(
    output.folder(dataset=dataset,problem='classify'),
    'xgboost.model')
  
  start <- proc.time()
  n_proc <- detectCores()
  md <- xgboost(
    data=X_train, 
    label=ifelse(dtrain[,response]=='Y',1,0),
    nthread=n_proc, 
    nround=1, 
    max_depth=maxdepth,
    min_child_weight=mincount,
    num_parallel_tree=ntrees, 
    subsample=0.632,
    colsample_bytree=
      1.0 / sqrt(length(X_train@x)/nrow(X_train)),
    save_name=modelfile,
    tree_method='exact',
    callbacks=list(cb.save.model(save_name=modelfile)))
  traintime <- proc.time() - start
  
  start <- proc.time()
  phat <- predict(md, newdata=X_test)
  truth <- dtest[,response]
  truth <- as.numeric(ifelse(truth[,response]=='Y',1,0))
  prtr <- data.frame(prediction=phat,truth=as.numeric(truth))
  predicttime <- proc.time() - start
  
  write.tsv(
    data=prtr, 
    file=predicted.file(
      prefix=paste('xgboost.exact',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, dtest[,response])
  auc <- performance(rocr_pred, 'auc')
  auctime <- proc.time() - start
  
  list(
    model='xgboost',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    auctime=auctime['elapsed'],
    auc=auc@y.values[[1]])
}
#-----------------------------------------------------------------
# l2 scalar regression
l2.xgboost.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.numeric(dtrain[[response]]),
    is.numeric(dtest[[response]]))
  
  set.seed(169544)
  
  start <- proc.time()
  
  X_train_test <- sparse.model.matrix(
    as.formula(paste(response, '~ .-1')), 
    data=rbind(dtrain, dtest))
  X_train <- X_train_test[1:nrow(dtrain),]
  X_test <- 
    X_train_test[(nrow(dtrain)+1):(nrow(dtrain)+nrow(dtest)),]
  
  datatime <- proc.time() - start
  
  modelfile <-file.path(
    output.folder(dataset=dataset,problem='l2'),
    'xgboost.model')
  
  start <- proc.time()
  n_proc <- detectCores()
  md <- xgboost(
    data=X_train, 
    label=ifelse(dtrain[,response]=='Y',1,0),
    nthread=n_proc, 
    nround=1, 
    max_depth=maxdepth,
    min_child_weight=mincount,
    num_parallel_tree=ntrees, 
    subsample=0.632,
    colsample_bytree=
      1.0 / sqrt(length(X_train@x)/nrow(X_train)),
    save_name=modelfile,
    callbacks=list(cb.save.model(save_name=modelfile)))
  traintime <- proc.time() - start
  
  start <- proc.time()
  phat <- predict(md, newdata=X_test)
  predicttime <- proc.time() - start
  
  prtr <- data.frame(
    prediction=phat,
    truth=dtest[[response]])
  start <- proc.time()
  rmse <- sqrt(mean((prtr$truth-prtr$prediction)^2))
  rmsetime <- proc.time() - start
  
  write.tsv(
    data=prtr, 
    file=predicted.file(
      prefix=paste('xgboost.exact',suffix,sep='-'),
      dataset=dataset,
      problem='l2'))
  
  list(
    model='xgboost',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    rmsetime=rmsetime['elapsed'],
    rmse=rmse)
}
#-----------------------------------------------------------------
# l2 scalar regression
l2.xgboost.exact.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.numeric(dtrain[[response]]),
    is.numeric(dtest[[response]]))
  
  set.seed(169544)
  
  start <- proc.time()
  
  X_train_test <- sparse.model.matrix(
    as.formula(paste(response, '~ .-1')), 
    data=rbind(dtrain, dtest))
  X_train <- X_train_test[1:nrow(dtrain),]
  X_test <- 
    X_train_test[(nrow(dtrain)+1):(nrow(dtrain)+nrow(dtest)),]
  
  datatime <- proc.time() - start
  
  modelfile <-file.path(
    output.folder(dataset=dataset,problem='l2'),
    'xgboost.model')
  
  start <- proc.time()
  n_proc <- detectCores()
  md <- xgboost(
    data=X_train, 
    label=ifelse(dtrain[,response]=='Y',1,0),
    nthread=n_proc, 
    nround=1, 
    max_depth=maxdepth,
    min_child_weight=mincount,
    num_parallel_tree=ntrees, 
    subsample=0.632,
    colsample_bytree=
      1.0 / sqrt(length(X_train@x)/nrow(X_train)),
    save_name=modelfile,
    tree_method='exact',
    callbacks=list(cb.save.model(save_name=modelfile)))
  traintime <- proc.time() - start
  
  start <- proc.time()
  phat <- predict(md, newdata=X_test)
  predicttime <- proc.time() - start
  
  prtr <- data.frame(
    prediction=phat,
    truth=dtest[[response]])
  start <- proc.time()
  rmse <- sqrt(mean((prtr$truth-prtr$prediction)^2))
  rmsetime <- proc.time() - start
  
  write.tsv(
    data=prtr, 
    file=predicted.file(
      prefix=paste('xgboost.exact',suffix,sep='-'),
      dataset=dataset,
      problem='l2'))
  
  list(
    model='xgboost',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    rmsetime=rmsetime['elapsed'],
    rmse=rmse)
}
#-----------------------------------------------------------------
# R randomForest
#-----------------------------------------------------------------
# binary classification
classify.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.factor(dtrain[[response]]),
    is.factor(dtest[[response]]))
  
  set.seed(1244985)
  
  start <- proc.time()
  
  X <- model.matrix(
    as.formula(paste(response,' ~ .')), 
    data=rbind(dtrain, dtest))
  X_train <- X[1:nrow(dtrain),]
  i0 <- (nrow(dtrain)+1)
  i1 <- (nrow(dtrain)+nrow(dtest))
  X_test <- X[i0:i1,]
  
  datatime <- proc.time() - start
  
  start <- proc.time()
  forest <- randomForest(
    x=X_train,
    y=dtrain[[response]], 
    ntree=ntrees, 
    nodesize=mincount)
  traintime <- proc.time() - start   
  
  start <- proc.time()
  phat <- predict(forest, newdata=X_test, type='prob')[,'Y']
  predicttime <- proc.time() - start   
  
  write.tsv(
    data=data.frame(
      prediction=phat,
      truth=ifelse(dtest[[response]]=='Y',1,0)),
    file=predicted.file(
      prefix=paste('randomForest',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, dtest[[response]])
  auc <- performance(rocr_pred, 'auc')
  print(auc)
  auctime <- proc.time() - start
  
  list(
    model='randomForest',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    auctime=auctime['elapsed'],
    auc=auc@y.values[[1]])
}
#-----------------------------------------------------------------
# l2 scalar regression
l2.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.numeric(dtrain[[response]]),
    is.numeric(dtest[[response]]))
  
  set.seed(1244985)
  
  start <- proc.time()
  
  X <- model.matrix(
    as.formula(paste(response,' ~ .')), 
    data=rbind(dtrain, dtest))
  X_train <- X[1:nrow(dtrain),]
  i0 <- (nrow(dtrain)+1)
  i1 <- (nrow(dtrain)+nrow(dtest))
  X_test <- X[i0:i1,]
  
  datatime <- proc.time() - start
  
  start <- proc.time()
  forest <- randomForest(
    x=X_train,
    y=dtrain[[response]], 
    ntree=ntrees, 
    nodesize=mincount)
  traintime <- proc.time() - start   
  
  start <- proc.time()
  phat <- predict(forest, newdata=X_test)
  predicttime <- proc.time() - start
  
  prtr <- data.frame(
    prediction=phat,
    truth=dtest[[response]])
  start <- proc.time()
  rmse <- sqrt(mean((prtr$truth-prtr$prediction)^2))
  rmsetime <- proc.time() - start
  
  write.tsv(
    data=prtr,
    file=predicted.file(
      prefix=paste('randomForest',suffix,sep='-'),
      dataset=dataset,
      problem='l2'))
  
  list(
    model='randomForest',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    rmsetime=rmsetime['elapsed'],
    rmse=rmse)
}
#-----------------------------------------------------------------
# multicore training for R randomForest
#-----------------------------------------------------------------
# runs serially on windows; might be possible to use 
# parLapply instead of mclapply. See
# https://stat.ethz.ch/R-manual/R-devel/library/parallel/doc/parallel.pdf
classify.parallel.randomForest <- function (
  dataset=NULL,
  dtrain=NULL,
  suffix=NULL,
  dtest=NULL,
  response=NULL,
  ntrees=255,
  mincount=17,
  maxdepth=1024) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(dtrain),
    !is.null(suffix),
    !is.null(dtest),
    !is.null(response),
    is.factor(dtrain[[response]]),
    is.factor(dtest[[response]]))
  
  set.seed(1244985)
  
  start <- proc.time()
  datatime <- proc.time() - start
  
  ## 'Can not handle categorical predictors with more than 53 
  ## categories.'
  ## so need dummy variables/1-hot encoding
  ## - but then RF does not treat them as 1 variable
  
  X_train_test <-  model.matrix(
    as.formula(paste(response,' ~ .')), 
    data=rbind(dtrain, dtest))
  X_train <- X_train_test[1:nrow(dtrain),]
  i0 <- (nrow(dtrain)+1)
  i1 <- (nrow(dtrain)+nrow(dtest))
  X_test <- X_train_test[i0:i1,]
  
  
  dim(X_train)
  
  # 'mc.cores' > 1 is not supported on Windows
  start <- proc.time()
  n_proc <- detectCores()
  mds <- mclapply(
    1:n_proc,
    function(x) { 
      randomForest(
        X_train, 
        as.factor(dtrain[,response]), 
        nodesize=mincount, 
        ntree=floor(ntrees/n_proc))}, 
    mc.cores=n_proc)
  
  md <- do.call('combine', mds)
  traintime <- proc.time() - start   
  
  start <- proc.time()
  phat <- predict(md, newdata=dtest, type='prob')[,'Y']
  predicttime <- proc.time() - start   
  
  write.tsv(
    data=data.frame(
      prediction=phat,
      truth=ifelse(dtrain[,response]=='Y',1,0)),
    file=predicted.file(
      prefix=paste('parallel.randomForest',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, dtest[,response])
  auc <- performance(rocr_pred, 'auc')
  print(auc)
  auctime <- proc.time() - start
  
  #gc()
  #sapply(ls(),function(x) object.size(get(x))/1e6)
  list(
    model='parallel_randomForest',
    ntrain=nrow(dtrain),
    ntest=nrow(dtest),
    datatime=datatime['elapsed'],
    traintime=traintime['elapsed'],
    predicttime=predicttime['elapsed'],
    auctime=auctime['elapsed'],
    auc=auc@y.values[[1]])
}
#-----------------------------------------------------------------
# datasets
#-----------------------------------------------------------------
# just ensure factors 
ontime.data <- function (file=NULL) {
  data <- read_csv(file)
  data$cdayofweek <- as.factor(data$cdayofweek)
  data$cdayofmonth <- as.factor(data$cdayofmonth)
  data$cmonth <- as.factor(data$cmonth)
  data$uniquecarrier <- as.factor(data$uniquecarrier)
  data$dest <- as.factor(data$dest)
  data$origin <- as.factor(data$origin)
  data
}
# add binary response and remove scalar response
ontime.classify.data <- function (file=NULL) {
  data <- ontime.data(file=file)
  data$arr_delayed_15min <- as.factor(
    ifelse((data$arrdelay>=15),'Y','N'))
  #data[,!(names(data) %in% c('arrdelay'))]
  data[,which(names(data) != 'arrdelay')]
}
#-----------------------------------------------------------------
# benchmark loops
#-----------------------------------------------------------------
bench <- function (
  dataset=NULL,
  suffixes=NULL,
  dataf=NULL,
  trainf=NULL,
  dtest=NULL,
  response=NULL,
  problem=NULL,
  prefix=NULL) {
  
  results <- NULL
  for (suffix in suffixes) {
    gc()
    trainfile <-train.file(dataset=dataset,suffix=suffix)
    dtrain <- dataf(trainfile)
    resultf <- results.file(
      dataset=dataset,
      problem=problem,
      prefix=prefix)
    results <- rbind(
      results,
      trainf(
        dataset=dataset,
        dtrain=dtrain,
        suffix=suffix,
        dtest=dtest,
        response=response))
    print(results)
    resultsf <- 
      results.file(dataset=dataset,problem=problem,prefix=prefix)
    write.csv(
      results,
      file=resultsf,
      row.names=FALSE)
  } }
#-----------------------------------------------------------------

