package com.loopswork.loops.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Codi
 * @ClassName JsonObjectUtil
 * @Date: 2019/6/18 14:42
 */
public class JsonObjectUtil {

  /**
   * 根据需求对json格式的内容进行相关的更新
   *
   * @param json          待更新的json数据
   * @param operationType 更新的方式，有几种方式待选：remove, rename, replace, add, append。
   * @param values        待更新的值，比如["k1:v1";"k2:v2"]，如果是add操作，就表示分别将v1和v2插入到json，对应的key是k1和k2.
   */
  public static JsonObject transformerJson(JsonObject json, String operationType,
                                           List<String> values) {
    if (operationType == null) {
      return json;
    }
    switch (operationType) {
      case "remove":
        remove(json, values);
        break;
      case "rename":
        rename(json, values);
        break;
      case "replace":
        replace(json, values);
        break;
      case "add":
        add(json, values);
        break;
      case "append":
        append(json, values);
        break;
      default:
        break;
    }
    return json;
  }

  public static <T> List<T> json2ObjectList(List<JsonObject> list, Class<T> tClass) {
    return list.stream().map(jsonObject -> jsonObject2Object(jsonObject, tClass)).collect(Collectors.toList());
  }

  private static <T> T jsonObject2Object(JsonObject jsonObject, Class<T> tClass) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      return objectMapper.readValue(jsonObject.toString(), tClass);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 根据需求对map进行相关的更新
   *
   * @param map           待更新的map
   * @param operationType 更新的方式，有几种方式待选：remove, rename, replace, add, append。
   * @param values        待更新的值，比如["k1:v1";"k2:v2"]，如果是add操作，就表示分别将v1和v2插入到map，对应的key是k1和k2.
   * @return
   */
  public static Map<String, String> transformerMap(Map<String, String> map, String operationType,
                                                   List<String> values) {
    if (operationType == null) {
      return map;
    }
    switch (operationType) {
      case "remove":
        values.forEach(map::remove);
        break;
      case "rename":
        values.forEach(val -> {
          String[] strs = val.split(":");
          if (strs.length == 2) {
            if (map.containsKey(strs[0])) {
              map.put(strs[1], map.remove(strs[0]));
            }
          }
        });
        break;
      case "replace":
        values.forEach(val -> {
          String[] strs = val.split(":");
          if (strs.length == 2) {
            map.replace(strs[0], strs[1]);
          }
        });
        break;
      case "add":
        values.forEach(val -> {
          String[] strs = val.split(":");
          //提供一种读取模板变量的方法，然后根据模板变量的值进行对应的处理
          if (strs.length == 2) {
            String headerValue = handleHeaderTemplate(strs[1]);
            if (!map.containsKey(strs[0])) {
              map.put(strs[0], headerValue);
            }
          }
        });
        break;
      case "append":
        values.forEach(val -> {
          String[] strs = val.split(":");
          if (strs.length == 2) {
            map.put(strs[0], strs[1]);
          }
        });
        break;
      default:
        break;
    }
    return map;
  }

  private static String handleHeaderTemplate(String str) {
    str = str == null ? "" : str;
    if (str.contains(HEADER_TEMPLATE_ITEMS.UUID.item)) {
      //generate uuid，and construct req-uuid，then return
      UUID uuid = UUID.randomUUID();
      return "req-" + uuid;
    }
    return str;
  }

  /**
   * 对jsonobject执行add操作。values的格式：["a:1";"a.b.c:2"]
   * 如果对应的值已存在，则忽略该操作。
   */
  private static JsonObject add(JsonObject json, List<String> values) {
    values.forEach(val -> {
      // 使用:进行分割，第一部分表示执行操作的key,第二部分为待添加的值
      String[] strs = val.split(":", 2);
      if (strs.length == 2) {
        // 对key进行分层，比如key=a.b.c
        String[] vals = strs[0].split("\\.");
        if (vals.length == 1) {
          // 如果key只有一层，比如key=a，则只需要在body中添加即可。
          if (!json.containsKey(vals[0])) {
            json.put(vals[0], decodeValue(strs[1]));
          }
        } else {
          // 需要考虑 body = {"a":"b"},然后values["a.b"]的情况，此时json.getJsonObject 会抛出
          // java.lang.ClassCastException: java.lang.String cannot be cast to io.vertx.core.json.JsonObject
          // 如果key有多层，需要逐层处理，遇到某层在jsonobject中不存在，
          // 则需要添加新的jsonobject。
          JsonObject ephJson = null;
          if (!(json.getValue(vals[0]) instanceof JsonObject)) {
            ephJson = new JsonObject();
            json.put(vals[0], ephJson);
          } else {
            ephJson = json.getJsonObject(vals[0]);
          }
          {
            for (int i = 1; i < vals.length - 1; i++) {
              if (ephJson.getValue(vals[i]) == null) {
                ephJson.put(vals[i], new JsonObject());
              }
              if (ephJson.getValue(vals[i]) instanceof JsonObject) {
                ephJson = ephJson.getJsonObject(vals[i]);
              } else {
                // 对于key=a.b.c时，如果key=b的值不是jsonobject，则中断处理
                break;
              }
            }
            if (!ephJson.containsKey(vals[vals.length - 1])) {
              ephJson.put(vals[vals.length - 1], decodeValue(strs[1]));
            }
          }
        }
      }
    });
    return json;
  }

  /**
   * 对jsonobject执行append操作。values的格式：["a:1", "a.b.c:2"]
   * 如果对应的值已存在，则直接覆盖。
   */
  private static JsonObject append(JsonObject json, List<String> values) {
    values.forEach(val -> {
      // 使用:进行分割，第一部分表示执行操作的key,第二部分为待添加的值
      String[] strs = val.split(":", 2);
      if (strs.length == 2) {
        // 对key进行分层，比如key=a.b.c
        String[] vals = strs[0].split("\\.");
        if (vals.length == 1) {
          // 如果key只有一层，比如key=a，则只需要在body中添加即可。
          json.put(vals[0], decodeValue(strs[1]));
        } else {
          JsonObject ephJson = null;
          // 如果key有多层，需要逐层处理，遇到某层在jsonobject中不存在，
          // 则需要添加新的jsonobject。
          if (!(json.getValue(vals[0]) instanceof JsonObject)) {
            ephJson = new JsonObject();
            json.put(vals[0], ephJson);
          } else {
            ephJson = json.getJsonObject(vals[0]);
          }
          {
            for (int i = 1; i < vals.length - 1; i++) {
              // 如果中间层不是jsonobject，直接创建一个新的jsonobject覆盖即可。
              if (!(ephJson.getValue(vals[i]) instanceof JsonObject)) {
                ephJson.put(vals[i], new JsonObject());
              }
              ephJson = ephJson.getJsonObject(vals[i]);
            }
            ephJson.put(vals[vals.length - 1], decodeValue(strs[1]));
          }
        }
      }
    });
    return json;
  }

  /**
   * 对jsonobject执行remove操作。vlaue的格式: ["a","a.b"]
   */
  private static JsonObject remove(JsonObject json, List<String> values) {
    values.forEach(val -> {
      String[] vals = val.split("\\.");
      if (vals.length == 1) {
        json.remove(val);
      } else {
        JsonObject ephJson = json.getJsonObject(vals[0]);
        if (ephJson != null) {
          {
            for (int i = 1; i < vals.length - 1; i++) {
              if (ephJson.getValue(vals[i]) instanceof JsonObject) {
                ephJson = ephJson.getJsonObject(vals[i]);
              } else {
                break;
              }
            }
            ephJson.remove(vals[vals.length - 1]);
          }
        }
      }
    });
    return json;
  }

  /**
   * 对jsonobject执行replace操作，value的格式：["a:1", "a.b.c:2"]
   * 如果对应的key不存在，则忽略该操作
   */
  private static JsonObject replace(JsonObject json, List<String> values) {
    values.forEach(val -> {
      String[] strs = val.split(":", 2);
      if (strs.length == 2) {
        String[] vals = strs[0].split("\\.");
        if (vals.length == 1) {
          if (json.containsKey(vals[0])) {
            json.put(vals[0], decodeValue(strs[1]));
          }
        } else {
          if (json.getValue(vals[0]) instanceof JsonObject) {
            JsonObject ephJson = json.getJsonObject(vals[0]);
            {
              for (int i = 1; i < vals.length - 1; i++) {
                if (ephJson.getValue(vals[i]) instanceof JsonObject) {
                  ephJson = ephJson.getJsonObject(vals[i]);
                } else {
                  break;
                }
              }
              if (ephJson.containsKey(vals[vals.length - 1])) {
                ephJson.put(vals[vals.length - 1], decodeValue(strs[1]));
              }
            }
          }
        }
      }
    });
    return json;
  }

  /**
   * 对jsonobject执行rename操作，value的格式：["a:a1", "a.b.c:c1"]
   * 如果对应的key不存在，则忽略该操作
   */
  private static JsonObject rename(JsonObject json, List<String> values) {
    values.forEach(val -> {
      String[] strs = val.split(":", 2);
      if (strs.length == 2) {
        String[] vals = strs[0].split("\\.");
        if (vals.length == 1) {
          if (json.containsKey(vals[0])) {
            json.put(strs[1], json.remove(vals[0]));
          }
        } else {
          if (json.getValue(vals[0]) instanceof JsonObject) {
            JsonObject ephJson = json.getJsonObject(vals[0]);
            {
              for (int i = 1; i < vals.length - 1; i++) {
                if (ephJson.getValue(vals[i]) instanceof JsonObject) {
                  ephJson = ephJson.getJsonObject(vals[i]);
                } else {
                  break;
                }
              }
              if (ephJson.containsKey(vals[vals.length - 1])) {
                json.put(strs[1], ephJson.remove(vals[vals.length - 1]));
              }
            }
          }
        }
      }
    });
    return json;
  }

  /**
   * 对str的进行解析，并且转化为正确的类型：String,Long,List,Map.
   */
  private static Object decodeValue(String str) {
    try {
      return Json.decodeValue(str, Long.class);
    } catch (DecodeException ignored) {
    }
    try {
      return Json.decodeValue(str, new TypeReference<List<Object>>() {
      });
    } catch (DecodeException ignored) {
    }
    try {
      return Json.decodeValue(str, new TypeReference<Map<String, Object>>() {
      });
    } catch (DecodeException e2) {
      return str;
    }
  }

  /**
   * Request or response header template items
   */
  private enum HEADER_TEMPLATE_ITEMS {
    /*生成UUID*/
    UUID("{{UUID}}");

    public final String item;

    HEADER_TEMPLATE_ITEMS(String item) {
      this.item = item;
    }
  }
}
