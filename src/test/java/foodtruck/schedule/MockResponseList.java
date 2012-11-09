package foodtruck.schedule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class MockResponseList<T> implements ResponseList<T> {
  private List<T> statuses;

  public MockResponseList(List<T> statuses) {
    this.statuses = statuses;
  }

  @Override public RateLimitStatus getRateLimitStatus() {
    return null;
  }

  @Override public int getAccessLevel() {
    return 0;
  }

  @Override public int size() {
    return statuses.size();
  }

  @Override public boolean isEmpty() {
    return statuses.isEmpty();
  }

  @Override public boolean contains(Object o) {
    return statuses.contains(o);
  }

  @Override public Iterator<T> iterator() {
    return statuses.iterator();
  }

  @Override public Object[] toArray() {
    return statuses.toArray();
  }

  @Override public <T> T[] toArray(T[] ts) {
    return statuses.toArray(ts);
  }

  @Override public boolean add(T t) {
    return statuses.add(t);
  }

  @Override public boolean remove(Object o) {
    return statuses.remove(o);
  }

  @Override public boolean containsAll(Collection<?> objects) {
    return statuses.containsAll(objects);
  }

  @Override public boolean addAll(Collection<? extends T> ts) {
    return statuses.addAll(ts);
  }

  @Override public boolean addAll(int i, Collection<? extends T> ts) {
    return statuses.addAll(i, ts);
  }

  @Override public boolean removeAll(Collection<?> objects) {
    return statuses.removeAll(objects);
  }

  @Override public boolean retainAll(Collection<?> objects) {
    return statuses.retainAll(objects);
  }

  @Override public void clear() {
    statuses.clear();
  }

  @Override public T get(int i) {
    return statuses.get(i);
  }

  @Override public T set(int i, T t) {
    return statuses.set(i, t);
  }

  @Override public void add(int i, T t) {
    statuses.add(i, t);
  }

  @Override public T remove(int i) {
    return statuses.remove(i);
  }

  @Override public int indexOf(Object o) {
    return statuses.indexOf(o);
  }

  @Override public int lastIndexOf(Object o) {
    return statuses.lastIndexOf(o);
  }

  @Override public ListIterator<T> listIterator() {
    return statuses.listIterator();
  }

  @Override public ListIterator<T> listIterator(int i) {
    return statuses.listIterator(i);
  }

  @Override public List<T> subList(int i, int i1) {
    return statuses.subList(i, i1);
  }
}