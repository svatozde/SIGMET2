package cz.cvut.sigmet;


import android.util.FloatMath;



public class PointL{
    public long x;
    public long y;
    
    public PointL() {}

    public PointL(long x, long y) {
        this.x = x;
        this.y = y; 
    }
    
    public PointL(PointL p) { 
        this.x = p.x;
        this.y = p.y;
    }
    
  
    public final void set(long x, long y) {
        this.x = x;
        this.y = y;
    }
    
    public final void set(PointL p) { 
        this.x = p.x;
        this.y = p.y;
    }
    
    public final void negate() { 
        x = -x;
        y = -y; 
    }
    
    public final void offset(long dx, long dy) {
        x += dx;
        y += dy;
    }
    
  
    public final boolean equals(long x, long y) { 
        return this.x == x && this.y == y; 
    }

  
    public final long length() { 
        return length(x, y); 
    }
    
   
    public static long length(long x, long y) {
        return (long) FloatMath.sqrt(x * x + y * y);
    }

   

  
}
