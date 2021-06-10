# Alfresco - Override Auditable

This Repository Extension adds an _admin-only_ Endpoint for overriding the normally _read-only_ properties of the ``cm:auditable`` aspect.

Useful during/after migrations where those properties could not be set.

## Usage

```text
PUT /s/auditable/override
```

```json
[
  {
    "nodeId": "11111111-2222-3333-4444-555555555555",
    "properties": {
      "cm:created": "2000-01-01T00:00:00.000+00:00",
      "cm:creator": "alice",
      "cm:modified": "2007-03-06T01:23:45.678+00:00",
      "cm:modifier": "bob",
      "cm:accessed": "2021-11-21T11:22:33.444+00:00"
    }
  },
  {
    "nodeId": "66666666-7777-8888-9999-000000000000",
    "properties": {
      "cm:created": "2000-01-02T00:00:00.000+00:00"
    }
  }
]
```