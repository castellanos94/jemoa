# Libraries
library(readr)
library(ggplot2)
np<-7
baseDir <- "~/Documents/jemoa/experiments/nsga3dp/0/"
dir <- paste(baseDir,"/DTLZ",np,"_P/",sep="")
report <- read_csv(paste(dir,"report.csv",sep = ""))

png(file=paste(baseDir,"evolution-dtlz",np,".png",sep=""), width = 867, height = 553)

ggplot(report, aes(x=report$`Iteration / 50.0`, y=report$`N-Front / 50.0`)) +
  geom_line(aes(color="Fronts")) +
  geom_line(aes(y=report$`HSat / 50.0`, color="HSat")) +
  geom_line(aes(y=report$`Sat / 50.0`, color="Sat")) +
  ggtitle(paste("Evolution of DTLZ",np,"sets")) +
  labs(x="Iteration", y = "Count") +
  scale_color_manual(name="Sets",
                     breaks = c("Fronts","HSat","Sat"),
                     values = c("Fronts"="black","HSat"="red","Sat"="blue"))
dev.off()

