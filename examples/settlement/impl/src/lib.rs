//! Point-in-time cash-account snapshots with edition-correct settlement.
//!
//! Implements the `001-cash-snapshot` spec: a snapshot reports two books —
//! IBOR (traded cash: every fill executed by the as-of date) and ABOR
//! (settled cash: only trades whose cash has actually settled). Settlement
//! timing follows the standard cycle **in force on each trade's trade date**
//! (T+2 before the 2024-05-28 T+1 compliance date, T+1 on/after) — the domain
//! fact grounded in the `finance-settlement` corpus (KB-001).
//!
//! Money is exact `i64` cents; dates are a std-only proleptic-Gregorian day
//! count. No external dependencies.

/// A calendar date as days since 1970-01-01 — total order for free.
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Debug)]
pub struct Date(i64);

impl Date {
    pub fn from_ymd(y: i64, m: u32, d: u32) -> Date {
        Date(days_from_civil(y, m, d))
    }
    pub fn to_ymd(self) -> (i64, u32, u32) {
        civil_from_days(self.0)
    }
    fn next(self) -> Date {
        Date(self.0 + 1)
    }
    /// 0 = Sunday … 6 = Saturday.
    fn weekday(self) -> i64 {
        (self.0 + 4).rem_euclid(7)
    }
    fn is_weekend(self) -> bool {
        matches!(self.weekday(), 0 | 6)
    }
}

/// Howard Hinnant's civil ⇄ day-count algorithms (public domain).
fn days_from_civil(y0: i64, m: u32, d: u32) -> i64 {
    let y = if m <= 2 { y0 - 1 } else { y0 };
    let era = (if y >= 0 { y } else { y - 399 }) / 400;
    let yoe = y - era * 400;
    let (mm, dd) = (m as i64, d as i64);
    let doy = (153 * (if mm > 2 { mm - 3 } else { mm + 9 }) + 2) / 5 + dd - 1;
    let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    era * 146097 + doe - 719468
}

fn civil_from_days(z0: i64) -> (i64, u32, u32) {
    let z = z0 + 719468;
    let era = (if z >= 0 { z } else { z - 146096 }) / 146097;
    let doe = z - era * 146097;
    let yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    let y = yoe + era * 400;
    let doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    let mp = (5 * doy + 2) / 153;
    let d = (doy - (153 * mp + 2) / 5 + 1) as u32;
    let m = (if mp < 10 { mp + 3 } else { mp - 9 }) as u32;
    (if m <= 2 { y + 1 } else { y }, m, d)
}

/// A settlement calendar: weekends plus a set of market holidays.
pub struct SettlementCalendar {
    holidays: Vec<Date>,
}

impl SettlementCalendar {
    pub fn new(holidays: Vec<Date>) -> Self {
        SettlementCalendar { holidays }
    }

    pub fn is_business_day(&self, d: Date) -> bool {
        !d.is_weekend() && !self.holidays.contains(&d)
    }

    pub fn add_business_days(&self, from: Date, n: u32) -> Date {
        let mut cur = from;
        let mut added = 0;
        while added < n {
            cur = cur.next();
            if self.is_business_day(cur) {
                added += 1;
            }
        }
        cur
    }

    /// The settlement date for a US-equity trade: trade date + N business
    /// days, where N is the standard cycle in force on the trade date.
    /// KB-0001@2024-05-28 (T+1) supersedes KB-0001@2017-09-05 (T+2); a trade is
    /// settled under the edition current when it was struck — point-in-time
    /// correctness, not today's rule applied retroactively.
    pub fn settlement_date(&self, trade_date: Date) -> Date {
        self.add_business_days(trade_date, standard_cycle(trade_date))
    }
}

/// The US-equities T+1 compliance date (SEC Rule 15c6-1).
pub const T1_EFFECTIVE: Date = Date(19871); // 2024-05-28

/// Standard settlement cycle (business days) in force on `trade_date`.
pub fn standard_cycle(trade_date: Date) -> u32 {
    if trade_date < T1_EFFECTIVE {
        2 // T+2 — KB-0001@2017-09-05
    } else {
        1 // T+1 — KB-0001@2024-05-28
    }
}

/// Exact money in whole cents. Positive = cash in, negative = cash out.
pub type Cents = i64;

#[derive(Clone, Copy, PartialEq, Eq, Debug)]
pub enum Side {
    Buy,
    Sell,
}

#[derive(Clone, Copy, Debug)]
pub struct Trade {
    pub ticker: &'static str,
    pub side: Side,
    pub qty: i64,
    pub price_cents: Cents,
    pub trade_date: Date,
}

impl Trade {
    /// Cash effect of the trade: a buy pays out, a sell brings cash in.
    pub fn cash(&self) -> Cents {
        let gross = self.qty * self.price_cents;
        match self.side {
            Side::Buy => -gross,
            Side::Sell => gross,
        }
    }
}

/// A point-in-time snapshot of a single cash account.
#[derive(Clone, Copy, PartialEq, Eq, Debug)]
pub struct Snapshot {
    /// Investment Book of Record — traded cash (every fill by the as-of date).
    pub ibor_cents: Cents,
    /// Accounting Book of Record — settled cash only.
    pub abor_cents: Cents,
}

impl Snapshot {
    /// Cash in flight: traded but not yet settled.
    pub fn unsettled_cents(&self) -> Cents {
        self.ibor_cents - self.abor_cents
    }
}

/// Compute the cash-account snapshot as of `as_of` over a trade `book`.
///
/// IBOR includes every trade with `trade_date <= as_of`; ABOR includes only
/// trades whose settlement date (per the edition-correct cycle) is `<= as_of`.
pub fn snapshot(
    opening_cents: Cents,
    book: &[Trade],
    as_of: Date,
    cal: &SettlementCalendar,
) -> Snapshot {
    let mut ibor = opening_cents;
    let mut abor = opening_cents;
    for t in book {
        if t.trade_date <= as_of {
            ibor += t.cash();
        }
        if cal.settlement_date(t.trade_date) <= as_of {
            abor += t.cash();
        }
    }
    Snapshot {
        ibor_cents: ibor,
        abor_cents: abor,
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn cal() -> SettlementCalendar {
        // US market holidays relevant to the window (Memorial Day 2024).
        SettlementCalendar::new(vec![Date::from_ymd(2024, 5, 27)])
    }

    fn book() -> Vec<Trade> {
        vec![
            Trade { ticker: "AMZN", side: Side::Sell, qty: 100, price_cents: 18_000, trade_date: Date::from_ymd(2024, 5, 23) },
            Trade { ticker: "AAPL", side: Side::Buy,  qty: 100, price_cents: 19_000, trade_date: Date::from_ymd(2024, 5, 24) },
            Trade { ticker: "MSFT", side: Side::Sell, qty: 200, price_cents: 43_000, trade_date: Date::from_ymd(2024, 5, 28) },
            Trade { ticker: "NVDA", side: Side::Buy,  qty: 150, price_cents:  9_500, trade_date: Date::from_ymd(2024, 5, 30) },
        ]
    }

    #[test]
    fn the_t1_effective_constant_is_2024_05_28() {
        assert_eq!(T1_EFFECTIVE, Date::from_ymd(2024, 5, 28));
        // and 2024-05-27 really is a Monday (Memorial Day).
        assert_eq!(Date::from_ymd(2024, 5, 27).weekday(), 1);
    }

    #[test]
    fn cycle_is_edition_correct_around_the_cutover() {
        assert_eq!(standard_cycle(Date::from_ymd(2024, 5, 24)), 2); // last T+2 trade date
        assert_eq!(standard_cycle(Date::from_ymd(2024, 5, 28)), 1); // first T+1 trade date
    }

    #[test]
    fn settlement_dates_span_the_double_settlement_day() {
        let c = cal();
        // A trade struck before the cutover settles T+2 (the rule then in force);
        // one struck on the cutover settles T+1 — and both land on 2024-05-29.
        assert_eq!(c.settlement_date(Date::from_ymd(2024, 5, 24)), Date::from_ymd(2024, 5, 29)); // AAPL, T+2
        assert_eq!(c.settlement_date(Date::from_ymd(2024, 5, 28)), Date::from_ymd(2024, 5, 29)); // MSFT, T+1
        // AMZN's earlier T+2 trade settles on the cutover day itself.
        assert_eq!(c.settlement_date(Date::from_ymd(2024, 5, 23)), Date::from_ymd(2024, 5, 28));
        // NVDA, struck after the cutover, settles T+1.
        assert_eq!(c.settlement_date(Date::from_ymd(2024, 5, 30)), Date::from_ymd(2024, 5, 31));
    }

    #[test]
    fn ibor_and_abor_diverge_then_reconcile() {
        let (c, b) = (cal(), book());
        let opening = 10_000_000; // $100,000.00

        // As of the cutover: AMZN has settled; AAPL and MSFT are traded but
        // unsettled, so the books disagree by exactly that in-flight cash.
        let s28 = snapshot(opening, &b, Date::from_ymd(2024, 5, 28), &c);
        assert_eq!(s28.ibor_cents, 18_500_000); // $185,000.00
        assert_eq!(s28.abor_cents, 11_800_000); // $118,000.00
        assert_eq!(s28.unsettled_cents(), 6_700_000); // AAPL −$19k + MSFT +$86k

        // The next day, AAPL and MSFT settle together — the gap closes.
        let s29 = snapshot(opening, &b, Date::from_ymd(2024, 5, 29), &c);
        assert_eq!(s29.ibor_cents, 18_500_000);
        assert_eq!(s29.abor_cents, 18_500_000);
        assert_eq!(s29.unsettled_cents(), 0);
    }

    #[test]
    fn a_buy_pays_out_and_a_sell_brings_cash_in() {
        let buy = Trade { ticker: "AAPL", side: Side::Buy, qty: 100, price_cents: 19_000, trade_date: T1_EFFECTIVE };
        let sell = Trade { ticker: "MSFT", side: Side::Sell, qty: 200, price_cents: 43_000, trade_date: T1_EFFECTIVE };
        assert_eq!(buy.cash(), -1_900_000);
        assert_eq!(sell.cash(), 8_600_000);
    }
}
