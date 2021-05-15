#install.packages("scatterplot3d") # Install
library("scatterplot3d") # load
library(readr)
n <- 7
problem <- paste("DTLZ",n, sep ="")
baseDir = "/home/thinkpad/Documents/jemoa/experiments/3/NSGA3/FRONT_PREFERENCES/"
FRONT_PREFERENCES  <- read_csv(paste(baseDir,problem,"_ALL.csv",sep=""), col_types = cols(Class = col_factor(levels = c("HSAT", 
                                                                                                                          "SAT", "DIS", "HDIS"))))
FRONT_PREFERENCES$Algorithm <- as.factor(FRONT_PREFERENCES$Algorithm)

shapes = c(16,16,16,16,16) 
shapes <- shapes[as.numeric(FRONT_PREFERENCES$Algorithm)]

colors <- c("#33fc00", "#0083fe", "#fea500","#ff0019")
colors <- colors[as.numeric(FRONT_PREFERENCES$Class)]
angle <- 165
if(n == 1){
  angle <- 145
}
if(n == 2 || n == 5 || n == 6){
  angle = 25
}
png(file=paste(baseDir,"3D-",problem,".png",sep=""), width = 1280 , height = 720 )

s3d <-scatterplot3d(FRONT_PREFERENCES[,1:3],
              main=paste(problem,""),pch = shapes, angle = angle, color=colors,grid=TRUE, box=TRUE)
s3d$points3d(FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=='ROI',][,1:3], pch = shapes,col=c("#000000"))

legend("right", legend = c(levels(FRONT_PREFERENCES$Class),"ROI"),
       col =  c("#33fc00", "#0083fe", "#fea500","#ff0019","#000000"), pch = shapes, cex=0.95)

dev.off()



png(file=paste(baseDir,"3D-",problem,"-algorithm.png",sep=""), width = 1280 , height = 720 )
shapes <- shapes[as.numeric(FRONT_PREFERENCES$Algorithm)]

colors <- c("#33fc00", "#0083fe", "#fea500","#ff0019", "#5e5e5e","#000000")
colors <- colors[as.numeric(FRONT_PREFERENCES$Algorithm)]
s3d <-scatterplot3d(FRONT_PREFERENCES[,1:3],
              main=paste("3D Scatter",problem,"plot"),pch = shapes, angle = angle, color=colors,grid=TRUE, box=TRUE)
s3d$points3d(FRONT_PREFERENCES[FRONT_PREFERENCES$Algorithm=='ROI',][,1:3], pch = shapes,col=c("#000000"))

legend("right", legend = c(levels(FRONT_PREFERENCES$Algorithm)),
       col =  c("#33fc00", "#0083fe", "#fea500","#ff0019", "#5e5e5e","#000000"), pch = shapes,cex=1.6)
dev.off()
