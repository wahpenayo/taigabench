# wahpenayo at gmail dot com
# since 2016-01-31
# 2017-11-20
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
#-------------------------------------------------------------------------------

results <- NULL
for (prefix in 
  c('h2o','randomForest','scikit-learn','taiga','xgboost')) {
  f <- results.file(
    dataset='ontime',
    problem='classify',
    prefix=prefix)
  results <- rbind(results,read_csv(file=f))
}

results$model <- factor(results$model,levels=models)

dev.on(
  file=plot.file(
    dataset='ontime',
    problem='classify',
    prefix='traintime'),
  aspect=0.5,
  width=1280)
ggplot(results, aes(x = ntrain, y = traintime, color = model)) +
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
ggplot(results, aes(x = ntrain, y = auc, color = model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10)))  +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("higher is better")
dev.off()

#-------------------------------------------------------------------------------
