package chat.rocket.core.utils;

public class Triple<F, S, T> {

  public final F first;
  public final S second;
  public final T third;

  public Triple(F first, S second, T third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Triple)) {
      return false;
    }
    Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
    return equals(t.first, first) && equals(t.second, second)
        && equals(t.third, third);
  }

  @Override
  public int hashCode() {
    return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode())
        ^ (third == null ? 0 : third.hashCode());
  }

  private boolean equals(Object var0, Object var1) {
    return var0 == var1 || var0 != null && var0.equals(var1);
  }

  public static <A, B, C> Triple<A, B, C> create(A a, B b, C c) {
    return new Triple<A, B, C>(a, b, c);
  }
}
