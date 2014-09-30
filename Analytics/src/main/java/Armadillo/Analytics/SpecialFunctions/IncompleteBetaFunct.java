package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class IncompleteBetaFunct
{
    public static double IncompleteBeta(double a, double b, double x)
    {
    	try{
        double result = 0;
        double t = 0;
        double xc = 0;
        double w = 0;
        double y = 0;
        int flag = 0;
        double[] sg = new double[1];
        double big = 0;
        double biginv = 0;
        double maxgam = 0;
        double minlog = 0;
        double maxlog = 0;

        big = 4.503599627370496e15;
        biginv = 2.22044604925031308085e-16;
        maxgam = 171.624376956302725;
        minlog = Math.log(
            MathConstants.MIN_REAL_NUMBER);
        maxlog = Math.log(
            MathConstants.MAX_REAL_NUMBER);
        if (a < 0 || b < 0)
        {
            //Debugger.Break();
            throw new HCException("Domain error in IncompleteBeta");
        }
        if (x < 0 || x > 1)
        {
            //Debugger.Break();
            throw new HCException("Domain error in IncompleteBeta: " + x);
        }
        if (x == 0)
        {
            result = 0;
            return result;
        }
        if (x == 1)
        {
            result = 1;
            return result;
        }
        flag = 0;
        if (b*x <= 1.0 & x <= 0.95)
        {
            result = incompletebetaps(a, b, x, maxgam);
            return result;
        }
        w = 1.0 - x;
        if (x > a/(a + b))
        {
            flag = 1;
            t = a;
            a = b;
            b = t;
            xc = x;
            x = w;
        }
        else
        {
            xc = w;
        }
        if (flag == 1 & b*x <= 1.0 & x <= 0.95)
        {
            t = incompletebetaps(a, b, x, maxgam);
            if (t <= MathConstants.MACHINE_EPSILON)
            {
                result = 1.0 - MathConstants.MACHINE_EPSILON;
            }
            else
            {
                result = 1.0 - t;
            }
            return result;
        }
        y = x*(a + b - 2.0) - (a - 1.0);
        if (y < 0.0)
        {
            w = incompletebetafe(a, b, x, big, biginv);
        }
        else
        {
            w = incompletebetafe2(a, b, x, big, biginv)/xc;
        }
        y = a*Math.log(x);
        t = b*Math.log(xc);
        if (a + b < maxgam & Math.abs(y) < maxlog & Math.abs(t) < maxlog)
        {
            t = Math.pow(xc, b);
            t = t*Math.pow(x, a);
            t = t/a;
            t = t*w;
            t = t*(GammaFunct.Gamma(a + b)/(GammaFunct.Gamma(a)*GammaFunct.Gamma(b)));
            if (flag == 1)
            {
                if (t <= MathConstants.MACHINE_EPSILON)
                {
                    result = 1.0 - MathConstants.MACHINE_EPSILON;
                }
                else
                {
                    result = 1.0 - t;
                }
            }
            else
            {
                result = t;
            }
            return result;
        }
        y = y + t + LnGammaFunct.LnGamma(a + b, sg) -
            LnGammaFunct.LnGamma(a, sg) -
            LnGammaFunct.LnGamma(b, sg);
        y = y + Math.log(w/a);
        if (y < minlog)
        {
            t = 0.0;
        }
        else
        {
            t = FastMath.exp(y);
            if (Double.isInfinite(t))
            {
                t = Double.MAX_VALUE;
            }
        }
        if (flag == 1)
        {
            if (t <= MathConstants.MACHINE_EPSILON)
            {
                t = 1.0 - MathConstants.MACHINE_EPSILON;
            }
            else
            {
                t = 1.0 - t;
            }
        }
        result = t;
        return result;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    public static double InvIncompleteBeta(
        double a,
        double b,
        double y)
    {
    	try{
        double result = 0;
        double aaa = 0;
        double bbb = 0;
        double y0 = 0;
        double d = 0;
        double yyy = 0;
        double x = 0;
        double x0 = 0;
        double x1 = 0;
        double lgm = 0;
        double yp = 0;
        double di = 0;
        double dithresh = 0;
        double yl = 0;
        double yh = 0;
        double xt = 0;
        int i = 0;
        int rflg = 0;
        int dir = 0;
        int nflg = 0;
        double[] s = new double[1];
        int mainlooppos = 0;
        int ihalve = 0;
        int ihalvecycle = 0;
        int newt = 0;
        int newtcycle = 0;
        int breaknewtcycle = 0;
        int breakihalvecycle = 0;

        i = 0;
        if (!(y >= 0 & y <= 1))
        {
            //Debugger.Break();
            throw new HCException("Domain error in InvIncompleteBeta");
        }
        if (y == 0)
        {
            result = 0;
            return result;
        }
        if (y == 1.0)
        {
            result = 1;
            return result;
        }
        x0 = 0.0;
        yl = 0.0;
        x1 = 1.0;
        yh = 1.0;
        nflg = 0;
        mainlooppos = 0;
        ihalve = 1;
        ihalvecycle = 2;
        newt = 3;
        newtcycle = 4;
        breaknewtcycle = 5;
        breakihalvecycle = 6;
        while (true)
        {
            //
            // start
            //
            if (mainlooppos == 0)
            {
                if (a <= 1.0 | b <= 1.0)
                {
                    dithresh = 1.0e-6;
                    rflg = 0;
                    aaa = a;
                    bbb = b;
                    y0 = y;
                    x = aaa/(aaa + bbb);
                    yyy = IncompleteBeta(aaa, bbb, x);
                    mainlooppos = ihalve;
                    continue;
                }
                else
                {
                    dithresh = 1.0e-4;
                }
                yp = -InvNormalFunct.InvNormal(y);
                if (y > 0.5)
                {
                    rflg = 1;
                    aaa = b;
                    bbb = a;
                    y0 = 1.0 - y;
                    yp = -yp;
                }
                else
                {
                    rflg = 0;
                    aaa = a;
                    bbb = b;
                    y0 = y;
                }
                lgm = (yp*yp - 3.0)/6.0;
                x = 2.0/(1.0/(2.0*aaa - 1.0) + 1.0/(2.0*bbb - 1.0));
                d = yp*Math.sqrt(x + lgm)/x -
                    (1.0/(2.0*bbb - 1.0) - 1.0/(2.0*aaa - 1.0))*(lgm + 5.0/6.0 - 2.0/(3.0*x));
                d = 2.0*d;
                if (d < Math.log(MathConstants.MIN_REAL_NUMBER))
                {
                    x = 0;
                    break;
                }
                x = aaa/(aaa + bbb*FastMath.exp(d));
                yyy = IncompleteBeta(aaa, bbb, x);
                yp = (yyy - y0)/y0;
                if (Math.abs(yp) < 0.2)
                {
                    mainlooppos = newt;
                    continue;
                }
                mainlooppos = ihalve;
                continue;
            }

            //
            // ihalve
            //
            if (mainlooppos == ihalve)
            {
                dir = 0;
                di = 0.5;
                i = 0;
                mainlooppos = ihalvecycle;
                continue;
            }

            //
            // ihalvecycle
            //
            if (mainlooppos == ihalvecycle)
            {
                if (i <= 99)
                {
                    if (i != 0)
                    {
                        x = x0 + di*(x1 - x0);
                        if (x == 1.0)
                        {
                            x = 1.0 - MathConstants.MACHINE_EPSILON;
                        }
                        if (x == 0.0)
                        {
                            di = 0.5;
                            x = x0 + di*(x1 - x0);
                            if (x == 0.0)
                            {
                                break;
                            }
                        }
                        yyy = IncompleteBeta(aaa, bbb, x);
                        yp = (x1 - x0)/(x1 + x0);
                        if (Math.abs(yp) < dithresh)
                        {
                            mainlooppos = newt;
                            continue;
                        }
                        yp = (yyy - y0)/y0;
                        if (Math.abs(yp) < dithresh)
                        {
                            mainlooppos = newt;
                            continue;
                        }
                    }
                    if (yyy < y0)
                    {
                        x0 = x;
                        yl = yyy;
                        if (dir < 0)
                        {
                            dir = 0;
                            di = 0.5;
                        }
                        else
                        {
                            if (dir > 3)
                            {
                                di = 1.0 - (1.0 - di)*(1.0 - di);
                            }
                            else
                            {
                                if (dir > 1)
                                {
                                    di = 0.5*di + 0.5;
                                }
                                else
                                {
                                    di = (y0 - yyy)/(yh - yl);
                                }
                            }
                        }
                        dir = dir + 1;
                        if (x0 > 0.75)
                        {
                            if (rflg == 1)
                            {
                                rflg = 0;
                                aaa = a;
                                bbb = b;
                                y0 = y;
                            }
                            else
                            {
                                rflg = 1;
                                aaa = b;
                                bbb = a;
                                y0 = 1.0 - y;
                            }
                            x = 1.0 - x;
                            yyy = IncompleteBeta(aaa, bbb, x);
                            x0 = 0.0;
                            yl = 0.0;
                            x1 = 1.0;
                            yh = 1.0;
                            mainlooppos = ihalve;
                            continue;
                        }
                    }
                    else
                    {
                        x1 = x;
                        if (rflg == 1 & x1 < MathConstants.MACHINE_EPSILON)
                        {
                            x = 0.0;
                            break;
                        }
                        yh = yyy;
                        if (dir > 0)
                        {
                            dir = 0;
                            di = 0.5;
                        }
                        else
                        {
                            if (dir < -3)
                            {
                                di = di*di;
                            }
                            else
                            {
                                if (dir < -1)
                                {
                                    di = 0.5*di;
                                }
                                else
                                {
                                    di = (yyy - y0)/(yh - yl);
                                }
                            }
                        }
                        dir = dir - 1;
                    }
                    i = i + 1;
                    mainlooppos = ihalvecycle;
                    continue;
                }
                else
                {
                    mainlooppos = breakihalvecycle;
                    continue;
                }
            }

            //
            // breakihalvecycle
            //
            if (mainlooppos == breakihalvecycle)
            {
                if (x0 >= 1.0)
                {
                    x = 1.0 - MathConstants.MACHINE_EPSILON;
                    break;
                }
                if (x <= 0.0)
                {
                    x = 0.0;
                    break;
                }
                mainlooppos = newt;
                continue;
            }

            //
            // newt
            //
            if (mainlooppos == newt)
            {
                if (nflg != 0)
                {
                    break;
                }
                nflg = 1;
                lgm = LnGammaFunct.LnGamma(aaa + bbb, s) -
                      LnGammaFunct.LnGamma(aaa, s) -
                      LnGammaFunct.LnGamma(bbb, s);
                i = 0;
                mainlooppos = newtcycle;
                continue;
            }

            //
            // newtcycle
            //
            if (mainlooppos == newtcycle)
            {
                if (i <= 7)
                {
                    if (i != 0)
                    {
                        yyy = IncompleteBeta(aaa, bbb, x);
                    }
                    if (yyy < yl)
                    {
                        x = x0;
                        yyy = yl;
                    }
                    else
                    {
                        if (yyy > yh)
                        {
                            x = x1;
                            yyy = yh;
                        }
                        else
                        {
                            if (yyy < y0)
                            {
                                x0 = x;
                                yl = yyy;
                            }
                            else
                            {
                                x1 = x;
                                yh = yyy;
                            }
                        }
                    }
                    if (x == 1.0 | x == 0.0)
                    {
                        mainlooppos = breaknewtcycle;
                        continue;
                    }
                    d = (aaa - 1.0)*Math.log(x) + (bbb - 1.0)*Math.log(1.0 - x) + lgm;

                    if (d < Math.log(MathConstants.MIN_REAL_NUMBER)*10000)
                    {
                        break;
                    }
                    if (d > Math.log(MathConstants.MAX_REAL_NUMBER))
                    {
                        mainlooppos = breaknewtcycle;
                        continue;
                    }
                    d = FastMath.exp(d);
                    d = (yyy - y0)/d;
                    xt = x - d;
                    if (xt <= x0)
                    {
                        yyy = (x - x0)/(x1 - x0);
                        xt = x0 + 0.5*yyy*(x - x0);
                        if (xt <= 0.0)
                        {
                            mainlooppos = breaknewtcycle;
                            continue;
                        }
                    }
                    if (xt >= x1)
                    {
                        yyy = (x1 - x)/(x1 - x0);
                        xt = x1 - 0.5*yyy*(x1 - x);
                        if (xt >= 1.0)
                        {
                            mainlooppos = breaknewtcycle;
                            continue;
                        }
                    }
                    x = xt;
                    if (Math.abs(d/x) < 128.0*MathConstants.MACHINE_EPSILON)
                    {
                        break;
                    }
                    i = i + 1;
                    mainlooppos = newtcycle;
                    continue;
                }
                else
                {
                    mainlooppos = breaknewtcycle;
                    continue;
                }
            }

            //
            // breaknewtcycle
            //
            if (mainlooppos == breaknewtcycle)
            {
                dithresh = 256.0*MathConstants.MACHINE_EPSILON;
                mainlooppos = ihalve;
                continue;
            }
        }

        //
        // done
        //
        if (rflg != 0)
        {
            if (x <= MathConstants.MACHINE_EPSILON)
            {
                x = 1.0 - MathConstants.MACHINE_EPSILON;
            }
            else
            {
                x = 1.0 - x;
            }
        }
        result = x;
        return result;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

//public static double InverseIncompleteBeta(
//    double d, 
//    double d1, 
//    double d2, 
//    double d3)
//{
//    bool flag;
//    double d4;
//    double d8;
//    double d9;
//    double d10;
//    double d11;
//    double d13;
//    double d16;
//    double d21;
//    double d22;
//    d22 = d3;
//    if(d <= 0.0D || d1 <= 0.0D || d3 < 0.0D || d3 > 1.0D)
//        throw new ArgumentException("Invalid argument of inverse of incomplete beta function ratio.");
//    if(d3 == 0.0D || d3 == 1.0D)
//        return d3;
//    if(d3 <= 0.5D)
//    {
//        d4 = d3;
//        d8 = d;
//        d10 = d1;
//        flag = false;
//    } else
//    {
//        d4 = 1.0D - d3;
//        d8 = d1;
//        d10 = d;
//        flag = true;
//    }
//    d11 = Math.Sqrt(-Math.Log(d4 * d4));
//    double d19 = d11 - (2.3075299999999999D + 0.27061000000000002D * d11) / (1.0D + (0.99229000000000001D + 0.044810000000000003D * d11) * d11);
//    if(d8 > 1.0D && d10 > 1.0D)
//    {
//        d11 = (d19 * d19 - 3D) / 6D;
//        double d12 = 1.0D / ((d8 + d8) - 1.0D);
//        double d14 = 1.0D / ((d10 + d10) - 1.0D);
//        double d7 = 2D / (d12 + d14);
//        double d18 = (d19 * Math.Sqrt(d7 + d11)) / d7 - (d14 - d12) * ((d11 + 0.83333333333333337D) - 2D / (3D * d7));
//        d22 = d8 / (d8 + d10 * Math.Exp(d18 + d18));
//    } else
//    {
//        d11 = d10 + d10;
//        double d15 = 1.0D / (9D * d10);
//        d15 = d11 * Math.Pow((1.0D - d15) + d19 * Math.Sqrt(d15), 3D);
//        if(d15 <= 0.0D)
//        {
//            d22 = 1.0D - Math.Exp((Math.Log((1.0D - d4) * d10) + d2) / d10);
//        } else
//        {
//            d15 = ((4D * d8 + d11) - 2D) / d15;
//            if(d15 <= 1.0D)
//                d22 = Math.Exp((Math.Log(d4 * d8) + d2) / d8);
//            else
//                d22 = 1.0D - 2D / (d15 + 1.0D);
//        }
//    }
//    d11 = 1.0D - d8;
//    d16 = 1.0D - d10;
//    d21 = 0.0D;
//    d13 = 1.0D;
//    d9 = 1.0D;
//    if(d22 < 0.0001D)
//        d22 = 0.0001D;
//    if(d22 > 0.99990000000000001D)
//        d22 = 0.99990000000000001D;
//_L1:
//    double d6;
//    double d20;
//    d20 = incompleteBeta(d22, d8, d10, d2);
//    d20 = (d20 - d4) * Math.Exp(d2 + d11 * Math.Log(d22) + d16 * Math.Log(1.0D - d22));
//    if(d20 * d21 <= 0.0D)
//        d9 = d13;
//    d6 = 1.0D;
//_L2:
//    double d17;
//    do
//    {
//        double d5 = d6 * d20;
//        d13 = d5 * d5;
//        if(d13 >= d9)
//        {
//            d6 /= 3D;
//        } else
//        {
//label0:
//            {
//                d17 = d22 - d5;
//                if(d17 < 0.0D || d17 > 1.0D)
//                    break MISSING_BLOCK_LABEL_662;
//                if(d9 <= 9.9999999999999997E-029D || d20 * d20 <= 9.9999999999999997E-029D)
//                    return !flag ? d22 : 1.0D - d22;
//                if(d17 != 0.0D && d17 != 1.0D)
//                    break label0;
//                d6 /= 3D;
//            }
//        }
//    } while(true);
//    if(d17 == d22)
//        return !flag ? d22 : 1.0D - d22;
//    d22 = d17;
//    d21 = d20;
//      goto _L1
//    d6 /= 3D;
//      goto _L2
//}


    private static double incompletebetafe(double a,
                                           double b,
                                           double x,
                                           double big,
                                           double biginv)
    {
        double result = 0;
        double xk = 0;
        double pk = 0;
        double pkm1 = 0;
        double pkm2 = 0;
        double qk = 0;
        double qkm1 = 0;
        double qkm2 = 0;
        double k1 = 0;
        double k2 = 0;
        double k3 = 0;
        double k4 = 0;
        double k5 = 0;
        double k6 = 0;
        double k7 = 0;
        double k8 = 0;
        double r = 0;
        double t = 0;
        double ans = 0;
        double thresh = 0;
        int n = 0;

        k1 = a;
        k2 = a + b;
        k3 = a;
        k4 = a + 1.0;
        k5 = 1.0;
        k6 = b - 1.0;
        k7 = k4;
        k8 = a + 2.0;
        pkm2 = 0.0;
        qkm2 = 1.0;
        pkm1 = 1.0;
        qkm1 = 1.0;
        ans = 1.0;
        r = 1.0;
        n = 0;
        thresh = 3.0*MathConstants.MACHINE_EPSILON;
        do
        {
            xk = -(x*k1*k2/(k3*k4));
            pk = pkm1 + pkm2*xk;
            qk = qkm1 + qkm2*xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            xk = x*k5*k6/(k7*k8);
            pk = pkm1 + pkm2*xk;
            qk = qkm1 + qkm2*xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            if (qk != 0)
            {
                r = pk/qk;
            }
            if (r != 0)
            {
                t = Math.abs((ans - r)/r);
                ans = r;
            }
            else
            {
                t = 1.0;
            }
            if (t < thresh)
            {
                break;
            }
            k1 = k1 + 1.0;
            k2 = k2 + 1.0;
            k3 = k3 + 2.0;
            k4 = k4 + 2.0;
            k5 = k5 + 1.0;
            k6 = k6 - 1.0;
            k7 = k7 + 2.0;
            k8 = k8 + 2.0;
            if (Math.abs(qk) + Math.abs(pk) > big)
            {
                pkm2 = pkm2*biginv;
                pkm1 = pkm1*biginv;
                qkm2 = qkm2*biginv;
                qkm1 = qkm1*biginv;
            }
            if (Math.abs(qk) < biginv | Math.abs(pk) < biginv)
            {
                pkm2 = pkm2*big;
                pkm1 = pkm1*big;
                qkm2 = qkm2*big;
                qkm1 = qkm1*big;
            }
            n = n + 1;
        }
        while (n != 300);
        result = ans;
        return result;
    }


    private static double incompletebetafe2(double a,
                                            double b,
                                            double x,
                                            double big,
                                            double biginv)
    {
        double result = 0;
        double xk = 0;
        double pk = 0;
        double pkm1 = 0;
        double pkm2 = 0;
        double qk = 0;
        double qkm1 = 0;
        double qkm2 = 0;
        double k1 = 0;
        double k2 = 0;
        double k3 = 0;
        double k4 = 0;
        double k5 = 0;
        double k6 = 0;
        double k7 = 0;
        double k8 = 0;
        double r = 0;
        double t = 0;
        double ans = 0;
        double z = 0;
        double thresh = 0;
        int n = 0;

        k1 = a;
        k2 = b - 1.0;
        k3 = a;
        k4 = a + 1.0;
        k5 = 1.0;
        k6 = a + b;
        k7 = a + 1.0;
        k8 = a + 2.0;
        pkm2 = 0.0;
        qkm2 = 1.0;
        pkm1 = 1.0;
        qkm1 = 1.0;
        z = x/(1.0 - x);
        ans = 1.0;
        r = 1.0;
        n = 0;
        thresh = 3.0*MathConstants.MACHINE_EPSILON;
        do
        {
            xk = -(z*k1*k2/(k3*k4));
            if (xk < -1.0)
            {
                xk = -1.0;
            }
            pk = pkm1 + pkm2*xk;
            qk = qkm1 + qkm2*xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            xk = z*k5*k6/(k7*k8);
            pk = pkm1 + pkm2*xk;
            qk = qkm1 + qkm2*xk;
            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            if (qk != 0)
            {
                r = pk/qk;
            }
            if (r != 0)
            {
                t = Math.abs((ans - r)/r);
                ans = r;
            }
            else
            {
                t = 1.0;
            }
            if (t < thresh)
            {
                break;
            }
            k1 = k1 + 1.0;
            k2 = k2 - 1.0;
            k3 = k3 + 2.0;
            k4 = k4 + 2.0;
            k5 = k5 + 1.0;
            k6 = k6 + 1.0;
            k7 = k7 + 2.0;
            k8 = k8 + 2.0;
            if (Math.abs(qk) + Math.abs(pk) > big)
            {
                pkm2 = pkm2*biginv;
                pkm1 = pkm1*biginv;
                qkm2 = qkm2*biginv;
                qkm1 = qkm1*biginv;
            }
            if (Math.abs(qk) < biginv | Math.abs(pk) < biginv)
            {
                pkm2 = pkm2*big;
                pkm1 = pkm1*big;
                qkm2 = qkm2*big;
                qkm1 = qkm1*big;
            }
            n = n + 1;
        }
        while (n != 300);
        result = ans;
        return result;
    }


    private static double incompletebetaps(double a,
                                           double b,
                                           double x,
                                           double maxgam)
    {
        double result = 0;
        double s = 0;
        double t = 0;
        double u = 0;
        double v = 0;
        double n = 0;
        double t1 = 0;
        double z = 0;
        double ai = 0;
        double[] sg = new double[1];

        ai = 1.0/a;
        u = (1.0 - b)*x;
        v = u/(a + 1.0);
        t1 = v;
        t = u;
        n = 2.0;
        s = 0.0;
        z = MathConstants.MACHINE_EPSILON*ai;
        while (Math.abs(v) > z)
        {
            u = (n - b)*x/n;
            t = t*u;
            v = t/(a + n);
            s = s + v;
            n = n + 1.0;
        }
        s = s + t1;
        s = s + ai;
        u = a*Math.log(x);
        if (a + b < maxgam & Math.abs(u) < Math.log(MathConstants.MAX_REAL_NUMBER))
        {
            t = GammaFunct.Gamma(a + b)/
                (GammaFunct.Gamma(a)*GammaFunct.Gamma(b));
            s = s*t*Math.pow(x, a);
        }
        else
        {
            t = LnGammaFunct.LnGamma(a + b, sg) -
                LnGammaFunct.LnGamma(a, sg) -
                LnGammaFunct.LnGamma(b, sg) + u + Math.log(s);
            if (t < Math.log(MathConstants.MIN_REAL_NUMBER))
            {
                s = 0.0;
            }
            else
            {
                s = FastMath.exp(t);
            }
        }
        result = s;
        return result;
    }
}