# wahpenayo at gmail dot com
# 2018-01-02
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
prefixes <- c('h2o','randomForest','randomForestSRC',
  'scikit-learn','taiga','xgboost') 
#-----------------------------------------------------------------
classify <- NULL
for (prefix in prefixes) {
  f <- results.file(
    dataset='ontime',
    problem='classify',
    prefix=prefix)
  print(f)
  classify <- rbind(classify,read_csv(file=f))
}
classify$model <- factor(classify$model,levels=models)
dev.on(
  file=plot.file(
    dataset='ontime',problem='classify',
    prefix='classify-traintime'),
  aspect=0.5,
  width=1280)
ggplot(classify, aes(x = ntrain, y = traintime, color = model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10))) + 
  scale_y_log10() +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()
dev.on(
  file=plot.file(
    dataset='ontime',
    problem='classify',
    prefix="auc"),
  aspect=0.5,
  width=1280)
ggplot(classify, aes(x = ntrain, y = auc, color = model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10)))  +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("higher is better")
dev.off()
#-----------------------------------------------------------------
l2 <- NULL
for (prefix in prefixes) {
  f <- results.file(
    dataset='ontime',
    problem='l2',
    prefix=prefix)
  l2 <- rbind(l2,read_csv(file=f)) }
l2$model <- factor(l2$model,levels=models)
dev.on(
  file=plot.file(
    dataset='ontime',problem='l2',prefix='l2-traintime'),
  aspect=0.5,
  width=1280)
ggplot(l2, aes(x=ntrain,y=traintime,color=model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10))) + 
  scale_y_log10() +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()

dev.on(
  file=plot.file(
    dataset='ontime',
    problem='l2',
    prefix='rmse'),
  aspect=0.5,
  width=1280)
ggplot(l2, aes(x=ntrain, y=rmse, color=model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10)))  +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()
#-----------------------------------------------------------------
qcost <- read_csv(
  file=file.path('output','qcost','ontime','results.csv'))
qcost$model <- factor(qcost$model,levels=models)
dev.on(
  file=plot.file(
    dataset='ontime',problem='qcost',
    prefix='qcost-traintime'),
  aspect=0.5,
  width=1280)
ggplot(qcost, aes(x=ntrain,y=traintime,color=model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10))) + 
  scale_y_log10() +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()

dev.on(
  file=plot.file(
    dataset='ontime',
    problem='qcost',
    prefix='qcost'),
  aspect=0.5,
  width=1280)
ggplot(qcost, aes(x=ntrain, y=decilecost, color=model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10)))  +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()
#-----------------------------------------------------------------
