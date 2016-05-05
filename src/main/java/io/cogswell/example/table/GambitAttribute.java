package io.cogswell.example.table;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author iganev
 */
public class GambitAttribute implements Comparable<GambitAttribute> {

    public static final String TYPE_DATE = "Date";
    public static final String TYPE_BOOL = "Boolean";
    public static final String TYPE_INT = "Integer";
    public static final String TYPE_NUM = "Number";

    /**
     * Attribute name
     */
    protected String mName = "";

    /**
     * Attribute data type
     */
    protected String mDataType = "text";

    /**
     * Is part of the core attributes
     */
    protected boolean mCore = false;

    /**
     * Is attribute primary key for the namespace
     */
    protected boolean mCiid = false;

    /**
     * User defined value
     */
    protected String mValue;

    public GambitAttribute(String name, String dataType, boolean core, boolean ciid) {
        this.mName = name;
        this.mDataType = dataType;
        this.mCore = core;
        this.mCiid = ciid;
    }

    /**
     * Attribute name
     */
    public String getName() {
        return mName;
    }

    /**
     * Attribute name
     */
    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * Attribute data type
     */
    public String getDataType() {
        return mDataType;
    }

    /**
     * Attribute data type
     */
    public void setDataType(String mDataType) {
        this.mDataType = mDataType;
    }

    /**
     * Is part of the core attributes
     */
    public boolean isCore() {
        return mCore;
    }

    /**
     * Is part of the core attributes
     */
    public void setCore(boolean mCore) {
        this.mCore = mCore;
    }

    /**
     * Is attribute primary key for the namespace
     */
    public boolean isCiid() {
        return mCiid;
    }

    /**
     * Is attribute primary key for the namespace
     */
    public void setCiid(boolean mCiid) {
        this.mCiid = mCiid;
    }

    /**
     * User defined value
     */
    public String getValue() {
        return mValue;
    }

    /**
     * User defined value
     */
    public void setValue(String mValue) {
        this.mValue = mValue;
    }

    /**
     * Return JSON representation.
     *
     * @return
     */
    public JSONObject toJson() {
        return toJson(true);
    }

    /**
     * Return JSON representation.
     *
     * @param complete
     * @return
     */
    public JSONObject toJson(boolean complete) {
        JSONObject json = new JSONObject();

        try {
            json.put("name", getName());
                json.put("data_type", getDataType());

            json.put("value", getValue());

            if (complete) {
                json.put("ciid", isCiid());
                json.put("core", isCore());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Recreate an attribute saved to config.json
     *
     * @param json
     * @return
     */
    public static GambitAttribute fromJson(JSONObject json) {
        String name = null;
        String data_type = null;
        String value = null;

        boolean ciid = false;
        boolean core = false;

        try {
            name = json.getString("name");
            data_type = json.getString("data_type");
            value = json.getString("value");

            ciid = json.getBoolean("ciid");
            core = json.getBoolean("core");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GambitAttribute attr = new GambitAttribute(name, data_type, core, ciid);

        attr.setValue(value);

        return attr;
    }

    @Override
    public int compareTo(GambitAttribute o) {

        if (o.isCiid()) {
            return 1;
        }
        else {
            return getName().compareTo(o.getName());
        }
    }

    /**
     * Returns a type corrected value for JSON export.
     * @return
     */
    public Object getTypeCorrectedValue() {
        //Log.d("getDataType()", getDataType());
        //Log.d("getValue()", String.valueOf(getValue()));
        if (getDataType().equals(TYPE_BOOL)) {
            return new Boolean(getValue());
        }
        else if (getDataType().equals(TYPE_INT)) {
            return Integer.parseInt(getValue());
        }
        else if (getDataType().equals(TYPE_NUM)) {
            return Double.parseDouble(getValue());
        }
        else {
            return getValue();
        }
    }

}
