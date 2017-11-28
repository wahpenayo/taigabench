package taigabench.java.ontime;

public enum DayOfMonth {
  
  TSUITACHI,
  FUTSUKA,
  MIKKA,  
  YOKKA,  
  ITSUKA, 
  MUIKA,  
  NANOKA, 
  YOUKA,  
  KOKONOKA,
  DOOKA,   
  JUUICHINICHI,
  JUUNINICHI,  
  JUUSANNICHI, 
  JUUYONNICHI, 
  JUUGONICHI,  
  JUUROKUNICHI,
  JUUSHICHINICHI,
  JUUHACHINICHI, 
  JUUKUNICHI,  
  HATSUKA,   
  NIJUUICHINICHI,
  NIJUUNINICHI,  
  NIJUUSANNICHI, 
  NIJUUYONNICHI, 
  NIJUUGONICHI,  
  NIJUUROKUNICHI,
  NIJUUSHICHINICHI,
  NIJUUHACHINICHI, 
  NIJUUKUNICHI,  
  SANJUUNICHI,   
  SANJUUICHINICHI;

  // values() creates a new array on each call
  private static final DayOfMonth[] _vals = values();
  
  /** One-based lookup. */
  public static final DayOfMonth of (final int i) { 
    return _vals[i-1]; }

  /** One-based lookup. */
  public final int getValue () { return 1 + ordinal(); }
}
