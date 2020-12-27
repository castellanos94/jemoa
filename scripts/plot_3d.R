#install.packages("scatterplot3d") # Install
#install.packages(c("rgl", "car"))
library("car")
library("scatterplot3d") # load
source('http://www.sthda.com/sthda/RDoc/functions/addgrids3d.r')
library(readr)
problem <- "DTLZ1"
FRONT_PREFERENCES  <- read_csv(paste("/home/thinkpad/Documents/jemoa/experiments/FRONT_PREFERENCES_",problem,"_P.csv",sep=""),  col_types = cols(Problem = col_factor(levels = c(problem,  paste(problem,"_P",sep = ""), "ROI_PREFERENCES")), Class = col_factor(levels = c("HSAT",   "SAT", "DIS", "HDIS"))))
shapes = c(16, 17, 18) 
shapes <- shapes[as.numeric(FRONT_PREFERENCES$Problem)]

colors <- c("#f50000", "#E69F00", "#000000")
colors <- colors[as.numeric(FRONT_PREFERENCES$Problem)]
angle <- 45
# PROBLEMA
png(file=paste("/home/thinkpad/Documents/jemoa/experiments/",problem,"_solutions",".png",sep=""), width = 867, height = 553)
s3d <- scatterplot3d(FRONT_PREFERENCES[,1:3], pch = shapes, color=colors, angle = angle, box = FALSE)
addgrids3d(FRONT_PREFERENCES[, 1:3], grid = c("y", "xz", "yz"), angle = angle)
s3d$points3d(FRONT_PREFERENCES[FRONT_PREFERENCES$Problem=='ROI_PREFERENCES',][,1:3], pch=18,col=c("#000000"))

legend("right", legend = c("NSGA-III","NSGA-III PREFERENCES","ROI PREFERENCES"),
       col =  c("#f50000", "#E69F00", "#000000"), 
       pch = c(16,17,18))
dev.off()

# CLASE
colors_class <- c("#33fc00", "#0072fc", "#7e858c","#ff0019")
colors_class <- colors_class[as.numeric(FRONT_PREFERENCES$Class)]
shapes_class <- c(16, 17, 18,19) 
shapes_class <- shapes_class[as.numeric(FRONT_PREFERENCES$Class)]
png(file=paste("/home/thinkpad/Documents/jemoa/experiments/",problem,"_class",".png",sep=""), width = 867, height = 553)

s3d_class <- scatterplot3d(FRONT_PREFERENCES[,1:3], pch = shapes_class, color=colors_class, angle = angle, box = FALSE)
addgrids3d(FRONT_PREFERENCES[, 1:3], grid = c("y", "xz", "yz"), angle = angle)
roi <- FRONT_PREFERENCES[FRONT_PREFERENCES$Problem=='ROI_PREFERENCES',]
colors_class <- c("#33fc00", "#0072fc", "#7e858c","#ff0019")
colors_class <- colors_class[as.numeric(roi$Class)]
s3d_class$points3d(roi, pch=12,col=colors_class)

legend("right", legend = c("HSAT","SAT","ROI PREFERENCES"),
       col =  c("#33fc00", "#0072fc", "#000000","#ff0019"), 
       pch = c(16,17,12))
dev.off()

# Group Class 
#scatter3d(x = FRONT_PREFERENCES$`F-1`, y = FRONT_PREFERENCES$`F-2`, z = FRONT_PREFERENCES$`F-3`, groups = FRONT_PREFERENCES$Class, grid = FALSE, surface= FALSE, surface.col = c("#33fc00", "#0072fc", "#7e858c","#ff0019"))
# Group ROI Class
#roi <- FRONT_PREFERENCES[FRONT_PREFERENCES$Problem=='ROI_PREFERENCES',]
#scatter3d(x = roi$`F-1`, y = roi$`F-2`, z = roi$`F-3`, groups = roi$Class, grid = FALSE, surface= FALSE, surface.col = c("#33fc00", "#0072fc", "#7e858c","#ff0019"))
