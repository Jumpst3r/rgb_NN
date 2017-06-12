/********************************************************************************************
Camera Measurements

Description: Writes camera data to CSV files to create histogram.
Author: Beat Hirsbrunner, Julien Nembrini, Simon Studer (University of Fribourg)
Version: 1.0 (2016-03-24)
********************************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <webots/differential_wheels.h>
#include <webots/robot.h>
#include <webots/camera.h>

#define TIME_STEP   64

#define MAX_SAMPLES 10
#define WAIT_STEPS 5

WbDeviceTag cam;

int main(int argc, char **argv) {
  const unsigned char* im;
  int r,g,b;
  r = 0;
  g = 0;
  b = 0;
  int camera_width, camera_height;
  int i, m, n;


  wb_robot_init();
  cam = wb_robot_get_device("camera");
  wb_camera_enable(cam,4*TIME_STEP);
  camera_width = wb_camera_get_width(cam);
  camera_height = wb_camera_get_height(cam);

  // open files for writing
  FILE *csv = fopen("rgb2.csv", "w");
  int max = 900;
  for (i=0; i<max && wb_robot_step(TIME_STEP)!=-1; i++) {
  printf("%d\n",i);
        im = wb_camera_get_image(cam);
        for (n=0; n<camera_height; n++) {
          for (m=0;m<camera_width;m++) {
            r += wb_camera_image_get_red(im, camera_width, m, n);
            g += wb_camera_image_get_green(im, camera_width, m, n);
            b += wb_camera_image_get_blue(im, camera_width, m, n);
          }
        }
          r /= (camera_width * camera_height);
          g /= (camera_width * camera_height);
          b /= (camera_width * camera_height);
          printf("%d %d %d\n",r,g,b);
          fprintf(csv,"%d,%d,%d;", r,g,b);
          fprintf(csv,"1,0,0;\n");//add more columns if needed
          r = 0;
          g = 0;
          b =0;
  };

  // close files
  fclose(csv);


  wb_robot_cleanup();

  return EXIT_SUCCESS;
}
