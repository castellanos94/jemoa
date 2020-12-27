# Libraries
library(readr)
library(ggplot2)
library(viridisLite)
library(viridis)
library(hrbrthemes)
hrbrthemes::import_roboto_condensed()

report <- read_csv("~/Documents/jemoa/experiments/nsga3wp/report.csv")
report$algorithm <- as.factor(report$algorithm)
report$problem <- as.factor(report$problem)

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`N-Front / 50.0`, group = report$algorithm,color=report$algorithm)) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = T) +
  theme_ipsum() +
  ggtitle("Global evolution report by Algorithm")+
  labs(x="Iteration", y="N-Front")

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`HSat / 50.0`,group=report$algorithm, color=report$algorithm)) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = T) +
  theme_ipsum() +
  ggtitle("Global evolution report by Algorithm")+
  labs(x="Iteration", y="HSat")

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`Sat / 50.0`,group=report$algorithm, color=report$algorithm)) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = T) +
  theme_ipsum() +
  ggtitle("Global evolution report by Algorithm")+
  labs(x="Iteration", y="Sat") 

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`N-Front / 50.0`, color=report$problem)) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = T) +
  theme_ipsum() +
  ggtitle("Global evolution report by Problem")+
  labs(x="Iteration", y="N-Front")

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`HSat / 50.0`, group = report$problem,color=report$problem)) +
  #geom_area(alpha=0.6, size=.5) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = TRUE) +
  ggtitle("Global evolution report by Problem") +
  theme_ipsum() +
  labs(x="Iteration", y="HSat")

ggplot(report,aes(x=report$`Iteration / 50.0`,y=report$`Sat / 50.0`,group=report$problem, color=report$problem)) +
  geom_line(alpha=0.6) +
  scale_fill_viridis(discrete = T) +
  theme_ipsum() +
  ggtitle("Global evolution report by Problem")+
  labs(x="Iteration", y="Sat") 

# Current used
baseDir<-"~/Documents/jemoa/experiments/nsga3wp/"
png(file=paste(baseDir,"global-fronts-algorithm.png",sep=""), width = 3840 , height = 2160 )

ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`N-Front / 50.0`, group=algorithm, color=algorithm)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Algorithm") +
  theme_ipsum() +
  theme(
    legend.position="none",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="N-Front")
dev.off()

png(file=paste(baseDir,"global-hsat-algorithm.png",sep=""), width = 3840 , height = 2160 )
ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`HSat / 50.0`, group=algorithm, color=algorithm)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Algorithm") +
  theme_ipsum() +
  theme(
    legend.position="none",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="HSat")
dev.off()

png(file=paste(baseDir,"global-sat-algorithm.png",sep=""), width = 3840 , height = 2160 )
ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`Sat / 50.0`, group=algorithm, color=algorithm)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Algorithm") +
  theme_ipsum() +
  theme(
    legend.position="none",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="Sat")
dev.off()

png(file=paste(baseDir,"global-fronts-problem.png",sep=""), width = 3840 , height = 2160 )
ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`N-Front / 50.0`, group=problem, color=problem)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Problem") +
  theme_ipsum() +
  theme(
    legend.position="bottom",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="N-Front")
dev.off()

png(file=paste(baseDir,"global-hsat-problem.png",sep=""), width = 3840 , height = 2160 )
ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`HSat / 50.0`, group=problem, color=problem)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Problem") +
  theme_ipsum() +
  theme(
    legend.position="bottom",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="HSat")
dev.off()

png(file=paste(baseDir,"global-sat-problem.png",sep=""), width = 3840 , height = 2160 )
ggplot( report,aes(x=report$`Iteration / 50.0`, y=report$`Sat / 50.0`, group=problem, color=problem)) +
  geom_line() +
  scale_fill_viridis(discrete = TRUE) +
  theme(legend.position="none") +
  ggtitle("Global evolution report by Problem") +
  theme_ipsum() +
  theme(
    legend.position="bottom",
    panel.spacing = unit(0.1, "lines"),
    strip.text.x = element_text(size = 8),
    plot.title = element_text(size=14)
  ) +
  facet_wrap(~algorithm) + 
  labs(x="Iteration", y="Sat")
dev.off()
