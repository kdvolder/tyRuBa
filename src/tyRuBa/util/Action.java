package tyRuBa.util;

public interface Action {
  /** Execute action on an argument and return some kind of result */
  abstract public Object compute(Object arg);
}
