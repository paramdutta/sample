package org.my.konkurrent;

import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Write more tests
 */
public class ThreadPoolTest {

    @BeforeClass
    public static void disableLogging() {
        Utils.enableLog = false;
    }

    @Test(timeout = 10000)
    public void basicPoolTest() {
        ThreadPool pool = new ThreadPool(20, 2, true);

        try {
            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R1");
                }
            });

            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R2");
                }
            });


            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R3");
                }
            });

            int i = 0;
            while(i<100) {
                final int k = i;
                pool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("R" + k);
                    }
                });
                i++;
            }

            System.out.println("Done with while");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TestCase.assertTrue(pool.getInternalQueue().isEmpty());

        pool.stopThreadPool();
    }

    @Test(timeout = 10000)
    public void manyWorkersTest() {
        ThreadPool pool = new ThreadPool(20, 200, true);

        try {
            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R1");
                }
            });

            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R2");
                }
            });

            System.out.println("Sleeping 3 secs...");
            Thread.sleep(3000);

            pool.addTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("R3");
                }
            });

            int i = 0;
            while(i<100) {
                final int k = i;
                pool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("R" + k);
                    }
                });
                i++;
            }

            System.out.println("Done with while");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TestCase.assertTrue(pool.getInternalQueue().isEmpty());

        pool.stopThreadPool();
    }

    @Test(timeout = 10000)
    public void largeQueueWithFewWorkersTest() {
        ThreadPool pool = new ThreadPool(2000, 5, true);

        try {
            int i = 0;
            while(i<5000) {
                final int k = i;
                pool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("R" + k);
                    }
                });
                i++;
            }

            System.out.println("Done with while");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TestCase.assertTrue(pool.getInternalQueue().isEmpty());

        pool.stopThreadPool();
    }

    @Test(timeout = 10000)
    public void oneSlowTaskTest() {
        ThreadPool pool = new ThreadPool(200, 5, true);

        try {
            int i = 0;
            while(i<5000) {
                final int k = i;
                pool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("R" + k);
                    }
                });

                if(i == 500) {
                    pool.addTask(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Slow task is running.");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                i++;
            }



            System.out.println("Done with while");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TestCase.assertTrue(pool.getInternalQueue().isEmpty());

        pool.stopThreadPool();
    }
}
