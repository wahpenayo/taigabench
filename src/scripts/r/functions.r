# wahpenayo at gmail dot com
# 2017-11-20
#-----------------------------------------------------------------
# Load the necessary add-on packages, downloading and installing
# (in the user's R_LIBS_USER folder) if necessary.
load.packages <- function () {
  user.libs <- Sys.getenv('R_LIBS_USER')
  dir.create(user.libs,showWarnings=FALSE,recursive=TRUE)
  repos <- c('http://cran.fhcrc.org')
  packages <- 
    c('versions',
      'data.table', 
      'readr',
      'ROCR', 
      'randomForest',
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
# if ("package:h2o" %in% search()) { detach("package:h2o", unload=TRUE) }
# if ("h2o" %in% rownames(installed.packages())) { remove.packages("h2o") }
if (! eval(call('require','h2o',quietly=TRUE))) {
  install.packages(
    'h2o', 
    #Sys.getenv('R_LIBS_USER'),
    #type='source', 
    repos=(c('http://h2o-release.s3.amazonaws.com/h2o/latest_stable_R'))) 
}
#-----------------------------------------------------------------
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
data.folder <- function (
  dataset=NULL, 
  problem=NULL) {
  file.path('data',problem,dataset)
}

train.file <- function (
  dataset=NULL, 
  problem=NULL,
  suffix=NULL) {
  file.path(
    data.folder(problem=problem,dataset=dataset),
    paste("train-",suffix,".csv.gz",sep='')) }

test.file <- function (
  dataset=NULL, 
  problem=NULL) {
  file.path(
    data.folder(problem=problem,dataset=dataset),
    paste("test.csv.gz",sep='')) }

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
      paste(prefix,"pred.tsv.gz",sep='.'))) }

results.file <- function (
  dataset=NULL, 
  problem=NULL,
  prefix='all') {
  file.path(
    output.folder(problem=problem,dataset=dataset),
    paste(paste(prefix,"results.csv",sep='.'))) }

plot.file <- function (
  dataset=NULL, 
  problem=NULL,
  prefix) { 
  file.path(output.folder(problem=problem,dataset=dataset),prefix) }
#-----------------------------------------------------------------
models <- c(
  'h2o',
  'randomForest',
  'randomForestSRC',
  'scikit-learn',
  'taiga',
  'taiga-pfp',
  'taiga-mvp',
  'xgboost')
model.colors <- c(
  '#386cb050',
  '#1b9e7750',
  '#66a61e50',
  '#a6761d50',
  '#e41a1cFF',
  '#e41a1cFF',
  '#1a1ce4FF',
  '#75707050')
#-----------------------------------------------------------------
classify.h2o.randomForest <- function (
  dataset=NULL,
  trainfile=NULL,
  suffix=NULL,
  testfile=NULL,
  response=NULL) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(trainfile),
    !is.null(suffix),
    !is.null(testfile),
    !is.null(response))
  
  # H2O requires Java 8 as of 2017-11-17, version 3.15.0.4103
  old.java.home <- Sys.getenv('JAVA_HOME')
  Sys.setenv(JAVA_HOME=Sys.getenv('JAVA8'))
  
  set.seed(740189)
  h2o.init(max_mem_size="32g", nthreads=-1)
  
  start <- proc.time()
  dx_train <- h2o.importFile(path=trainfile)
  dx_test <- h2o.importFile(path=testfile)
  Xnames <- 
    names(dx_train)[which(names(dx_train)!=response)]
  datatime <- proc.time() - start
  
  start <- proc.time()
  md <- h2o.randomForest(
    x = Xnames, 
    y = response, 
    training_frame = dx_train, 
    ntrees = 500,
    min_rows = 10,
    max_depth = 20)
  traintime <- proc.time() - start
  
  #show(md)
  
  start <- proc.time()
  prediction <- as.data.frame(predict(md,dx_test))
  truth <- as.data.frame(dx_test[,response])
  truth <- ifelse(truth[,response]=='Y',1,0)
  prtr <- data.frame(
    prediction=prediction$Y,
    truth=as.numeric(truth))
  summary(prtr)
  predicttime <- proc.time() - start
  
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
# save mode callback that does nothing
cb.save.model <- function(
  save_period = 0, 
  save_name = "xgboost.model") {
  
  if (save_period < 0)
    stop("'save_period' cannot be negative")
  
  callback <- function(env = parent.frame()) {
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

classify.xgboost.randomForest <- function (
  dataset=NULL,
  trainfile=NULL,
  suffix=NULL,
  testfile=NULL,
  response=NULL) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(trainfile),
    !is.null(suffix),
    !is.null(testfile),
    !is.null(response))
  
  set.seed(169544)
  
  start <- proc.time()
  
  d_train <- read_csv(
    file=train.file(
      dataset=dataset,
      problem='classify',
      suffix=suffix))
  d_test <- read_csv(
    file=test.file(
      dataset=dataset,
      problem='classify'))
  
  X_train_test <- sparse.model.matrix(
    as.formula(paste(response, '~ .-1')), 
    data = rbind(d_train, d_test))
  X_train <- X_train_test[1:nrow(d_train),]
  X_test <- 
    X_train_test[(nrow(d_train)+1):(nrow(d_train)+nrow(d_test)),]
  #dim(X_train)
  
  datatime <- proc.time() - start
  
  modelfile <-file.path(
    output.folder(dataset=dataset,problem='classify'),
    'xgboost.model')
  # random forest with xgboost
  start <- proc.time()
  n_proc <- detectCores()
  md <- xgboost(
    data = X_train, 
    label = ifelse(d_train[,response]=='Y',1,0),
    nthread = n_proc, 
    nround = 1, 
    max_depth = 20,
    min_child_weight = 10,
    num_parallel_tree = 500, 
    subsample = 0.632,
    colsample_bytree = 
      1.0 / sqrt(length(X_train@x)/nrow(X_train)),
    save_name=modelfile,
    callbacks=list(cb.save.model(save_name=modelfile)))
  traintime <- proc.time() - start
  
  # can't figure out how to prevent this being saved.
  # at least want to be able to time training and model
  # serialization separately
  # file.remove(modelfile)
  
  start <- proc.time()
  phat <- predict(md, newdata = X_test)
  truth <- d_test[,response]
  truth <- as.numeric(ifelse(truth[,response]=='Y',1,0))
  prtr <- data.frame(prediction=phat,truth=as.numeric(truth))
  predicttime <- proc.time() - start
  
  write.tsv(
    data=prtr, 
    file=predicted.file(
      prefix=paste('xgboost',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, d_test[,response])
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
#-----------------------------------------------------------------
classify.parallel.randomForest <- function (
  dataset=NULL,
  trainfile=NULL,
  suffix=NULL,
  testfile=NULL,
  response=NULL) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(trainfile),
    !is.null(suffix),
    !is.null(testfile),
    !is.null(response))
  
  set.seed(1244985)
  
  start <- proc.time()
  d_train <- as.data.frame(
    read_csv(
      file=train.file(
        dataset=dataset,
        problem='classify',
        suffix=suffix)))
  d_test <- as.data.frame(
    read_csv(
      file=test.file(
        dataset=dataset,
        problem='classify')))
  datatime <- proc.time() - start
  
  print(summary(d_train))
  print(summary(d_test))
  ## "Can not handle categorical predictors with more than 53 
  ## categories."
  ## so need dummy variables/1-hot encoding
  ## - but then RF does not treat them as 1 variable
  
  X_train_test <-  model.matrix(
    as.formula(paste(response,' ~ .')), 
    data = rbind(d_train, d_test))
  X_train <- X_train_test[1:nrow(d_train),]
  i0 <- (nrow(d_train)+1)
  i1 <- (nrow(d_train)+nrow(d_test))
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
        as.factor(d_train[,response]), 
        ntree = floor(500/n_proc))}, 
    mc.cores = n_proc)
  
  md <- do.call("combine", mds)
  traintime <- proc.time() - start   
  
  start <- proc.time()
  phat <- predict(md, newdata = d_test, type = "prob")[,"Y"]
  predicttime <- proc.time() - start   
  
  write.tsv(
    data=data.frame(
      prediction=phat,
      truth=ifelse(d_train[,response]=='Y',1,0)),
    file=predicted.file(
      prefix=paste('parallel.randomForest',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, d_test[,response])
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
#-----------------------------------------------------------------
classify.randomForest <- function (
  dataset=NULL,
  trainfile=NULL,
  suffix=NULL,
  testfile=NULL,
  response=NULL) {
  
  stopifnot(
    !is.null(dataset),
    !is.null(trainfile),
    !is.null(suffix),
    !is.null(testfile),
    !is.null(response))
  
  set.seed(1244985)
  
  start <- proc.time()
  
  d_train <- as.data.frame(
    read_csv(
      file=train.file(
        dataset=dataset,
        problem='classify',
        suffix=suffix)))
  d_test <- as.data.frame(
    read_csv(
      file=test.file(
        dataset=dataset,
        problem='classify')))
  d <- rbind(d_train, d_test)
  X <- model.matrix(as.formula(paste(response,' ~ .')), data = d)
  X_train <- X[1:nrow(d_train),]
  i0 <- (nrow(d_train)+1)
  i1 <- (nrow(d_train)+nrow(d_test))
  print(c(i0,i1,nrow(X),nrow(d)))
  X_test <- X[i0:i1,]
  
  datatime <- proc.time() - start
  
  start <- proc.time()
  forest <- randomForest(x=X_train,
    y=as.factor(d_train[,response]), 
    ntree = 500, nodesize=10)
  traintime <- proc.time() - start   
  
  start <- proc.time()
  phat <- predict(forest, newdata = X_test, type = "prob")[,"Y"]
  predicttime <- proc.time() - start   
  
  write.tsv(
    data=data.frame(
      prediction=phat,
      truth=ifelse(d_test[,response]=='Y',1,0)),
    file=predicted.file(
      prefix=paste('randomForest',suffix,sep='-'),
      dataset=dataset,
      problem='classify'))
  
  start <- proc.time()
  rocr_pred <- prediction(phat, d_test[,response])
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
#-----------------------------------------------------------------
