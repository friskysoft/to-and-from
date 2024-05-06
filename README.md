# to-and-from

Simple object to object conversion library. Supports JSON, YAML and XML type conversions.

Inspiration: https://github.com/bazaarvoice/jolt

### Example

Input JSON:
```json
{
    "total": 99,
    "items": [
        {
            "id": "123",
            "name": "My Car",
            "category": "SUV",
            "quantity": "61",
            "details": {
                "color": "blue",
                "make": "Ford"
            }
        },
        {
            "id": "456",
            "name": "Another Car",
            "category": "SEDAN",
            "quantity": "13",
            "details": {
                "color": "blue",
                "make": "Cadillac"
            }
        }

    ]
}

```


Rule Set:
```yaml
output:
  summary:
    count:
      type: number
      from: total
  list:
    cars:
      - serial:
          from: items[].id
        name:
          from: items[].name
        group:
          from: items[].category
        count:
          from: items[].quantity
        color:
          from: items[].details.color
        make:
          from: items[].details.make


```


Output JSON:
```json
{
  "summary": {
    "count": 99
  },
  "list": {
    "cars": [
      {
        "serial": "123",
        "name": "My Car",
        "group": "SUV",
        "count": "61",
        "color": "blue",
        "make": "Ford"
      },
      {
        "serial": "456",
        "name": "Another Car",
        "group": "SEDAN",
        "count": "13",
        "color": "blue",
        "make": "Cadillac"
      }
    ]
  }
}
```

