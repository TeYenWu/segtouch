package com.example.simpleui.ringstudy1;

import android.text.style.LeadingMarginSpan;
import android.util.Log;

/**
 * Created by wudeyan on 8/25/16.
 */
public class SegTouch {
    double[] base  = new double[3];
    double[] middle = new double[3];
    double[] thumb = new double[3];
    double[] top = new double[3];

    /////////////////functions return//////////////
    double[] vector = new double[3];
    double[] unit = new double[3];
    double[] scale = new double[3];
    double[] add = new double[3];
    double[] sub = new double[3];
    double[] cross = new double[3];
//////////////////////////////////////////////

    //////////////vectors in pnt2line////////////
    double[] line_vec = new double[3];
    double[] pnt_vec = new double[3];
    double[] line_unitvec = new double[3];
    double[] pnt_vec_scaled = new double[3];
    double[] nearest = new double[3];
    double dist;
/////////////////////////////////////////////

    /////////////vectors in getposition////////////
    double[] nearestPointOnLine  = new double[3];
    double[] mv1 = new double[3];
    double[] mv2 = new double[3];
    double[] movecentral = new double[3];
    double[] movecentral_unitvec = new double[3];
    double[] movescale = new double[3];
    double[] center = new double[3];
    double[] yvector = new double[3];
    double[] vt = new double[3];
///////////////////////////////////////////////


    double x = 0, y = 0;
    double dans = 23.2, dia = 7.25; //input parameters
    int disth = 50; //distance threshold in mm
    double ymlwb = 49, ymupb = 84, yblwb = 40, ybupb = 85; //input parameter y upper and lower bounds in middle and base
    double y1 = (ymupb + ymlwb)/2, y2 = (ybupb + yblwb)/2;
    double m1 = (y2 - y1)/10;
    double y3 = ((ymupb - ymlwb)/3) + ymlwb, y4 = ((ybupb - yblwb)/3) + yblwb;
    double m2 = (y4 - y3)/10;
    double y5 = (((ymupb - ymlwb)/3) * 2) + ymlwb, y6 = (((ybupb - yblwb)/3) * 2) + yblwb;
    double m3 = (y6 - y5)/10;

    // finger mode
    int mode = 4; // mode 0: no segtouch, 1: 3, 2: 4, 3: 4+3, 4: 4+4, 5: 3+3+3

    double dot(double ax, double ay, double az, double bx, double by, double bz){
        return ((ax * bx) + (ay * by) + (az * bz));
    }

    double length(double ax, double ay, double az){
        return Math.sqrt((ax * ax) + (ay * ay) + (az * az));
    }

    void vector(double ax, double ay, double az, double bx, double by, double bz){
        vector = new double[3];
        vector[0] = bx - ax;
        vector[1] = by - ay;
        vector[2] = bz - az;
    }

    void unit(double ax, double ay, double az){
        unit = new double[3];
        double mag = length(ax, ay, az);
        unit[0] = ax/mag;
        unit[1] = ay/mag;
        unit[2] = az/mag;
    }

    double distance(double ax, double ay, double az, double bx, double by, double bz){
        vector(ax, ay, az, bx, by, bz);
        return length(vector[0], vector[1], vector[2]);
    }

    void scale(double ax, double ay, double az, double sc){
        scale = new double[3];
        scale[0] = ax * sc;
        scale[1] = ay * sc;
        scale[2] = az * sc;
    }

    void add(double ax, double ay, double az, double bx, double by, double bz){
        add = new double[3];
        add[0] = ax + bx;
        add[1] = ay + by;
        add[2] = az + bz;
    }

    void sub(double ax, double ay, double az, double bx, double by, double bz){
        sub = new double[3];
        sub[0] = ax + bx;
        sub[1] = ay + by;
        sub[2] = az + bz;
    }

    void pnt2line(double pntx, double pnty, double pntz, double startx, double starty, double startz, double endx, double endy, double endz){
        line_vec = new double[3];
        pnt_vec = new double[3];
        line_unitvec = new double[3];
        pnt_vec_scaled = new double[3];
        nearest = new double[3];
        dist = 0;

        vector(startx, starty, startz, endx, endy, endz);
        line_vec = vector;

        vector(startx, starty, startz, pntx, pnty, pntz);
        pnt_vec = vector;

        double line_len = length(line_vec[0], line_vec[1], line_vec[2]);

        unit(line_vec[0], line_vec[1], line_vec[2]);
        line_unitvec = unit;

        scale(pnt_vec[0], pnt_vec[1], pnt_vec[2], 1.0/line_len);
        pnt_vec_scaled = scale;

        double t = dot(line_unitvec[0], line_unitvec[1], line_unitvec[2], pnt_vec_scaled[0], pnt_vec_scaled[1], pnt_vec_scaled[2]);

        scale(line_vec[0], line_vec[1], line_vec[2], t); //projected vector
        nearest = scale;

        dist = distance(nearest[0], nearest[1], nearest[2], pnt_vec[0], pnt_vec[1], pnt_vec[2]);
//        print("distance: " +dist + "\n");

        add(nearest[0], nearest[1], nearest[2], startx, starty, startz); // normalized x
        nearest = add;
    }

    void cross(double ax, double ay, double az, double bx, double by, double bz){
        cross = new double[3];
        cross[0] = (ay * bz) - (by * az);
        cross[1] = (bx * az) - (ax * bz);
        cross[2] = (ax * by) - (bx * ay);
    }

    void getposition(){
        // middle translation (3)
        // base translation (3)
        // thumb translation (3)
        // object rotation (3)

        nearestPointOnLine = new double[3];
        pnt2line(thumb[0], thumb[1], thumb[2], middle[0], middle[1], middle[2], base[0], base[1], base[2]);
        nearestPointOnLine = nearest;


        //normalized x
        x = (distance(middle[0], middle[1], middle[2], nearestPointOnLine[0], nearestPointOnLine[1], nearestPointOnLine[2]) / (distance(middle[0], middle[1], middle[2], base[0], base[1], base[2]) /10.0));

        //normalized y
        mv1 = new double[3];
        mv2 = new double[3];
        vector(base[0], base[1], base[2], top[0], top[1], top[2]);
        mv1 = vector;
        Log.e("FF", String.valueOf(vector[2]));
        vector(base[0], base[1], base[2], middle[0], middle[1], middle[2]);
        mv2 = vector;
        Log.e("FF2", String.valueOf(vector[2]));
        movecentral = new double[3];
        cross(mv1[0], mv1[1], mv1[2], mv2[0], mv2[1], mv2[2]);
        movecentral = cross;

        movecentral_unitvec = new double[3];
        unit(movecentral[0], movecentral[1], movecentral[2]);
        movecentral_unitvec = unit;
        /////////////////////////////////////////////////////////////
        double movelength = (dans + 5) - dia;  //input move length in mm
//
        /////////////////////////////////////////////////////////////

        movescale = new double[3];
        scale(movecentral_unitvec[0], movecentral_unitvec[1], movecentral_unitvec[2], movelength);
        movescale = scale;

        center = new double[3];
        add(nearestPointOnLine[0], nearestPointOnLine[1], nearestPointOnLine[2], movescale[0], movescale[1], movescale[2]);
        center = add;

        vector(center[0], center[1], center[2], thumb[0], thumb[1], thumb[2]);
        yvector = vector;

        vt = new double[3];
        vector(base[0], base[1], base[2], top[0], top[1], top[2]);
        vt = vector;
        double ydot = dot(yvector[0], yvector[1], yvector[2], vt[0], vt[1], vt[2]);
        y = Math.toDegrees(Math.acos(ydot/(length(yvector[0], yvector[1], yvector[2]) * length(vt[0], vt[1], vt[2]))));
    }

    int segcursor(){


        int cur = -1;  //no cursor shown

        Log.e("debug XXXXXXX", String.valueOf(x));
        Log.e("debug YYYYYYY", String.valueOf(y));
        if(dist <= disth){
            double yth = y1 + (m1 * x); //y threshold
            double yth1 = y3 + (m2 * x); //y threshold 3 - 1
            double yth2 = y5 + (m3 * x); //y threshold 3 - 2
            if(mode == 0){
                cur = 0;
            }
            else if(mode == 1){  //1D 3 buttons
                double tmp = 10.0/3.0;
                if(x < tmp){
                    cur =  0;
                }
                else if((tmp <= x) && (x < (2 * tmp))){
                    cur = 1;
                }
                else if((2 * tmp) <= x){
                    cur = 2;
                }
            }
            else if(mode == 2){  //1D 4 buttons
                double tmp = 10.0/4.0;
                if(x < tmp){
                    cur =  0;
                }
                else if((tmp <= x) && (x < (2 * tmp))){
                    cur = 1;
                }
                else if(((2 * tmp) <= x) && (x < (3 * tmp))){
                    cur = 2;
                }
                else if((3 * tmp) <= x){
                    cur = 3;
                }
            }
            else if(mode == 3){  //3+3 buttons
                if(y < yth){
                    double tmp = 10.0/3.0;
                    if(x < tmp){
                        cur = 0;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 1;
                    }
                    else if((2 * tmp) <= x){
                        cur = 2;
                    }
                }
                else if(y >= yth){
                    double tmp = 10.0/3.0;
                    if(x < tmp){
                        cur = 3;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 4;
                    }
                    else if((2 * tmp) <= x){
                        cur = 5;
                    }
                }
            }
            else if(mode == 4){  //4+4 buttons
                if(y < yth){
                    double tmp = 10.0/4.0;
                    if(x < tmp){
                        cur = 0;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 1;
                    }
                    else if(((2 * tmp) <= x) && (x < (3 * tmp))){
                        cur = 2;
                    }
                    else if((3 * tmp) <= x){
                        cur = 3;
                    }
                }
                else if(y >= yth){
                    double tmp = 10.0/4.0;
                    if(x < tmp){
                        cur = 4;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 5;
                    }
                    else if(((2 * tmp) <= x) && (x < (3 * tmp))){
                        cur = 6;
                    }
                    else if((3 * tmp) <= x){
                        cur = 7;
                    }
                }
            }
            else if(mode == 5){  //3+3+3
                if(y < yth1){
                    double tmp = 10.0/3.0;
                    if(x < tmp){
                        cur = 0;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 1;
                    }
                    else if((2 * tmp) <= x){
                        cur = 2;
                    }
                }
                else if(yth1 <= y && y < yth2){
                    double tmp = 10.0/3.0;
                    if(x < tmp){
                        cur = 3;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 4;
                    }
                    else if((2 * tmp) <= x){
                        cur = 5;
                    }
                }
                else if(yth2 < y){
                    double tmp = 10.0/3.0;
                    if(x < tmp){
                        cur = 6;
                    }
                    else if((tmp <= x) && (x < (2 * tmp))){
                        cur = 7;
                    }
                    else if((2 * tmp) <= x){
                        cur = 8;
                    }
                }
            }
        }
        else{
            cur = -1;
        }
        return cur;
    }

    void setup(){
        middle[0] = 115.047778;
        middle[1] = -58.291351;
        middle[2] = 761.387865;

        base[0] = 121.028621;
        base[1] = -92.141674;
        base[2] = 804.555237;

        thumb[0] = 74.018567;
        thumb[1] = -74.773294;
        thumb[2] = 768.798023;

        top[0] = 109.604013;
        top[1] = -55.241005;
        top[2] = 839.272695;

        getposition();
//        print("x: " + x + "\ny: " + y + "\n");
        mode = 4;
        Log.e("debug XXXXXXXX", String.valueOf(x));
        Log.e("debug YYYYYYY", String.valueOf(y));
//        print("cursor: " + segcursor()+"\n");
    }
}
