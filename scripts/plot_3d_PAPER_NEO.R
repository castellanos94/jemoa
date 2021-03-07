library("scatterplot3d") # load
library(readr)
n <- 3
problem <- paste("DTLZ",n, sep ="")
baseDir = "/home/thinkpad/Documents/jemoa/experiments/NSGA3_last/FRONT_PREFERENCES/"
FRONT_PREFERENCES  <- read_csv(paste(baseDir,problem,"_P_ALL.csv",sep=""), col_types = cols(Class = col_factor(levels = c("HSAT", 
                                                                                                                          "SAT", "DIS", "HDIS"))))
FRONT_PREFERENCES$Algorithm <- as.factor(FRONT_PREFERENCES$Algorithm)

FC0R0 <- FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=="C0R0",]
FC100R0 <- FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=="C100R0",]

shapes = c(16,16,16,16,16) 
shapes <- shapes[as.numeric(FC100R0$Algorithm)]

colors <- c("#33fc00", "#0083fe", "#fea500","#ff0019")
colors <- colors[as.numeric(FC100R0$Class)]

angle <- 165
if(n == 1){
  angle <- 145
}
if(n == 2 || n == 5 || n == 6){
  angle = 25
}
# Font size

# c100r0
png(file=paste(baseDir,problem,"c100r0.png",sep=""), width = 1280 , height = 720 )

s3d<-scatterplot3d(FC100R0[,1:3],  main=paste(problem,"- C100R0"),pch = shapes, angle = angle, color=colors,grid=TRUE, box=FALSE)
s3d$points3d(FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=='ROI',][,1:3], pch = shapes,col=c("#000000"))
legend("right", legend = c(levels(FC100R0$Class),"ROI"), col =  c("#33fc00", "#0083fe", "#fea500","#ff0019","#000000"), pch = shapes, cex=2)
length(FC100R0[FC100R0$Class=="HSAT",]$Class)

dev.off()

#nsga3
shapes = c(16,16,16,16,16) 
shapes <- shapes[as.numeric(FC0R0$Algorithm)]

colors <- c("#33fc00", "#0083fe", "#fea500","#ff0019")
colors <- colors[as.numeric(FC0R0$Class)]
png(file=paste(baseDir,problem,"c0r0.png",sep=""), width = 1280 , height = 720 )

s3d<-scatterplot3d(FC0R0[,1:3],  main=paste(problem,"- C0R0"),pch = shapes, angle = angle, color=colors,grid=TRUE, box=FALSE)
s3d$points3d(FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=='ROI',][,1:3], pch = shapes,col=c("#000000"))
legend("right", legend = c(levels(FC0R0$Class),"ROI"), col =  c("#33fc00", "#0083fe", "#fea500","#ff0019","#000000"), pch = shapes,cex=2)
length(FC0R0[FC0R0$Class=="HSAT",]$Class)
dev.off()
