# John Alan McDonald 2017-01-31
#-------------------------------------------------------------------------------
setwd('e:/workplace/tu-projects/taigabench')
source('src/scripts/r/ontime/functions.r')
#-------------------------------------------------------------------------------

results <- NULL
results <- rbind(results,read.csv(file=results.file('h2o')))
results <- rbind(results,read.csv(file=results.file('r')))
results <- rbind(results,read.csv(file=results.file('scikit-learn')))
results <- rbind(results,read.csv(file=results.file('taiga')))
results <- rbind(results,read.csv(file=results.file('xgboost')))

results$model <- factor(results$model,levels=models)

dev.on(file=plot.file("traintime"),aspect=0.5,width=1280)
ggplot(results, aes(x = ntrain, y = traintime, color = model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10))) + 
  scale_y_log10() +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("lower is better")
dev.off()

dev.on(file=plot.file("auc"),aspect=0.5,width=1280)
ggplot(results, aes(x = ntrain, y = auc, color = model)) +
  geom_point(size=4.0) + 
  geom_line(size=2.0) + 
  scale_x_log10(breaks = (1000000*c(0.01,0.1,1,10)))  +
  scale_color_manual(values=(model.colors),drop=FALSE) +
  theme(text=element_text(size=24)) +
  ggtitle("higher is better")
dev.off()

#-------------------------------------------------------------------------------
