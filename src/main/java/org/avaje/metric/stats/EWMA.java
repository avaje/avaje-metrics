package org.avaje.metric.stats;

import static java.lang.Math.exp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An exponentially-weighted moving average.
 *
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg1.pdf">UNIX Load Average Part 1: How
 *      It Works</a>
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg2.pdf">UNIX Load Average Part 2: Not
 *      Your Average Average</a>
 */
class EWMA {
   
    
    private static final double MILLIS_PER_SEC = 1000d;
    private static final double SECONDS_PER_MINUTE = 60.0;
    private static final int ONE_MINUTE = 1;
    private static final int FIVE_MINUTES = 5;
    private static final int FIFTEEN_MINUTES = 15;
    private static final int INTERVAL = 2;
    private static final double M1_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / ONE_MINUTE);
    private static final double M5_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / FIVE_MINUTES);
    private static final double M15_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / FIFTEEN_MINUTES);

//    private final int minutes;
//    private final double origAlpha;
    private final double windowDuration;
    private final AtomicLong uncountedEvents = new AtomicLong();
    //private volatile boolean initialized = false;
    private volatile double rate = 0.0;
    
    private long lastUpdateTimeNanos;

    /**
     * Creates a new EWMA which is equivalent to the UNIX one minute load average and which expects
     * to be ticked every 5 seconds.
     *
     * @return a one-minute EWMA
     */
    public static EWMA oneMinuteEWMA() {
        return new EWMA(ONE_MINUTE, M1_ALPHA);
    }

    /**
     * Creates a new EWMA which is equivalent to the UNIX five minute load average and which expects
     * to be ticked every 5 seconds.
     *
     * @return a five-minute EWMA
     */
    public static EWMA fiveMinuteEWMA() {
        return new EWMA(FIVE_MINUTES, M5_ALPHA);
    }

    /**
     * Creates a new EWMA which is equivalent to the UNIX fifteen minute load average and which
     * expects to be ticked every 5 seconds.
     *
     * @return a fifteen-minute EWMA
     */
    public static EWMA fifteenMinuteEWMA() {
        return new EWMA(FIFTEEN_MINUTES, M15_ALPHA);
    }

    /**
     * Create a new EWMA with a specific smoothing constant.
     *
     * @param alpha        the smoothing constant
     * @param interval     the expected tick interval
     * @param intervalUnit the time unit of the tick interval
     */
    public EWMA(int minutes, double origAlpha) {
//      this.minutes = minutes;
//      this.origAlpha = origAlpha;
        this.windowDuration = MILLIS_PER_SEC * SECONDS_PER_MINUTE * (minutes*ONE_MINUTE);
    }

    public void clear() {
      uncountedEvents.set(0);
      rate = 0.0d;
    }
    
    /**
     * Update the moving average with a new value.
     *
     * @param n the new value
     */
    public void update(long n) {
        uncountedEvents.addAndGet(n);
    }


    public void updateAndTick(long n) {
        uncountedEvents.addAndGet(n);
        tick();
    }

    /**
     * Call this relatively often (every 2 to 10 seconds) to keep accurate.
     */
    public void tick() {

      final long eventCount = uncountedEvents.getAndSet(0);
      
        final long nowNanos = System.nanoTime();
        final long intervalNanos = nowNanos - lastUpdateTimeNanos;
        lastUpdateTimeNanos = nowNanos;
        
        final long intervalMillis = TimeUnit.NANOSECONDS.toMillis(intervalNanos);
        final double instantRate = (eventCount * MILLIS_PER_SEC) / intervalMillis;

        //if (initialized) {
            
//          double alpha3 = 1 - exp(-intervalMillis / 1000d / SECONDS_PER_MINUTE / (minutes*ONE_MINUTE));
            double alpha2 = 1 - exp(-intervalMillis / windowDuration);
            rate += (alpha2 * (instantRate - rate));
            if (eventCount > 0){
              //System.out.println("minutes:"+minutes+" alpha3:"+alpha3+" alpha2:"+alpha2+" origAlpha:"+origAlpha);
              System.out.println(" instantRate:"+instantRate+" rate:"+rate+" alpha2:"+alpha2);
            }
            
//        } else {
//            rate = instantRate;
//            initialized = true;
//        }
      
    }

    /**
     * Returns the rate in the given units of time.
     */
    public double getRate(TimeUnit rateUnit) {
      return rate * (double) rateUnit.toNanos(1);        
    }
}
