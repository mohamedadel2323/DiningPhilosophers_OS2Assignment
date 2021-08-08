package DiningPhilosophers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiningServerImpl implements DiningServer {

    // different philosopher states
    enum State {
        THINKING, HUNGRY, EATING
    };

    // number of philosophers
    public static final int NUM_OF_PHILS = 5;

    // array to record each philosopher's state
    private State[] state;
    private ReentrantLock lock;
    private Condition self[];

    public DiningServerImpl() {
        lock = new ReentrantLock();
        this.state = new State[NUM_OF_PHILS];
        this.self = new Condition[NUM_OF_PHILS];
        for (int i = 0; i < 5; i++) {
            state[i] = State.THINKING;
            self[i] = lock.newCondition();
        }
    }

    // called by a philosopher when they wish to eat 
    @Override
    public void takeForks(int pnum) {
        lock.lock();
        try {
            state[pnum] = State.HUNGRY;
            test(pnum);
            if (state[pnum] != State.EATING) {
                try {
                    self[pnum].await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DiningServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    // called by a philosopher when they are finished eating 
    @Override
    public void returnForks(int pnum) {
        lock.lock();
        try {
            state[pnum] = State.THINKING;
            test(leftNeighbour(pnum));
            test(rightNeighbour(pnum));
        } finally {
            lock.unlock();
        }
    }

    public void test(int pnum) {
        if ((state[leftNeighbour(pnum)] != State.EATING) && (state[pnum] == State.HUNGRY) && (state[rightNeighbour(pnum)] != State.EATING)) {
            state[pnum] = State.EATING;
        } else {
            self[pnum].signal();
        }
    }

    public int leftNeighbour(int pnum) {
        return ((pnum + 4) % NUM_OF_PHILS);
    }

    public int rightNeighbour(int pnum) {
        return ((pnum + 1) % NUM_OF_PHILS);
    }

}
