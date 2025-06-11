package org.example.server.manager;


import org.example.data.Worker;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import static java.lang.Math.max;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CollectionManager {
    private final LinkedList<Worker> workerLinkedList = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static int id;
    private static final HashSet<Integer> hs = new HashSet<>();

    public void add(Worker worker) {
        lock.writeLock().lock();
        try {
            if (hs.contains(worker.getId())) {
                worker.setId(id);
                id++;
            } else {
                id = max(worker.getId(), id);
                worker.setId(id);
                id++;
            }
            workerLinkedList.add(worker);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void shuffle() {
        lock.writeLock().lock();
        try {
            Collections.shuffle(workerLinkedList);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void reorder() {
        lock.writeLock().lock();
        try {
            Collections.reverse(workerLinkedList);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearCollection() {
        lock.writeLock().lock();
        try {
            workerLinkedList.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeId(int idToRemove) {
        lock.writeLock().lock();
        try {
            workerLinkedList.removeIf(w -> w.getId() == idToRemove);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeFirst() {
        lock.writeLock().lock();
        try {
            if (!workerLinkedList.isEmpty()) {
                workerLinkedList.removeFirst();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int averageSalary() {
        lock.readLock().lock();
        try {
            double total = 0.0;
            int count = workerLinkedList.size();
            for (Worker w : workerLinkedList) {
                total += w.getSalary();
            }
            return count == 0 ? 0 : (int) (total / count);
        } finally {
            lock.readLock().unlock();
        }
    }

    public LinkedList<Worker> getworkerLinkedList() {
        lock.readLock().lock();
        try {
            // возвращаем копию, чтобы внешний код не имел прямого доступа
            return new LinkedList<>(workerLinkedList);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return "Count of workers: " + workerLinkedList.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}