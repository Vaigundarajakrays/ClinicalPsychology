
---

# 📚 Case Study: Handling LOWER(bytea) Error in Hibernate 6 with JPA @ElementCollection

## 🔍 Initial Requirement

We needed to implement a **dynamic search API** for the `TherapistProfile` entity, supporting filters like:

* `name` — partial match, case-insensitive
* `location` — partial match, case-insensitive
* `price range` — `minPrice`, `maxPrice`
* `category` — partial match, case-insensitive, stored as a `List<String>` using `@ElementCollection`

---

## 📌 Initial Approach

We wrote the following JPA query assuming `categories` was an `@ElementCollection` of `List<String>`:

```java
@Query("""
SELECT DISTINCT t FROM TherapistProfile t 
JOIN t.categories c 
WHERE 
    (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND 
    (:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND 
    (:minPrice IS NULL OR t.amount >= :minPrice) AND 
    (:maxPrice IS NULL OR t.amount <= :maxPrice) AND 
    (:category IS NULL OR LOWER(c) LIKE LOWER(CONCAT('%', :category, '%')))
""")
List<TherapistProfile> searchTherapists(...);
```

---

## ❌ What Went Wrong

The query **worked initially**, but suddenly broke with the following PostgreSQL error:

```sql
ERROR: function lower(bytea) does not exist  
Hint: No function matches the given name and argument types. You might need to add explicit type casts.
```

It was caused by this line:

```sql
LOWER(c)
```

---

## 📦 Related Entity Mapping

```java
@ElementCollection
@CollectionTable(
    name = "therapist_profile_categories",
    joinColumns = @JoinColumn(name = "therapist_profile_id")
)
@Column(name = "category", columnDefinition = "TEXT")
private List<String> categories;
```

---

## 🧠 Root Cause

### Hibernate 6 Parameter Binding

* Hibernate 6 is **stricter** in how it binds query parameters.
* When `:category` is null, Hibernate **guesses the type**.
* It wrongly infers it as `bytea` instead of `TEXT`, causing `LOWER()` to fail (because it only works on strings).

> ⚠️ **Hibernate 5** used to bind nulls more forgivingly. Hibernate 6 uses **early and strict type binding** via Semantic Query Model (SQM).

---

## ✅ Solution

Instead of:

```sql
(:category IS NULL OR LOWER(c) LIKE LOWER(CONCAT('%', :category, '%')))
```

We use this **reversed null-check** pattern:

```java
@Query("""
SELECT DISTINCT t FROM TherapistProfile t 
JOIN t.categories c 
WHERE 
    (LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL) AND 
    (LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%')) OR :location IS NULL) AND 
    (t.amount >= :minPrice OR :minPrice IS NULL) AND 
    (t.amount <= :maxPrice OR :maxPrice IS NULL) AND 
    (LOWER(c) LIKE LOWER(CONCAT('%', :category, '%')) OR :category IS NULL)
""")
List<TherapistProfile> searchTherapists(...);
```

---

## 📚 Reference

Inspired by this helpful StackOverflow thread:
🔗 [https://stackoverflow.com/questions/77881433/org-postgresql-util-psqlexception-error-function-lowerbytea-does-not-exist](https://stackoverflow.com/questions/77881433/org-postgresql-util-psqlexception-error-function-lowerbytea-does-not-exist)

---

Last updated: 25/07/2025 
