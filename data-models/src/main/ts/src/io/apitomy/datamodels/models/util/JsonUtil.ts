
export class JsonUtil {

    public static keys(json: object): string[] {
        if (!json) {
            return [];
        }
        return Object.keys(json);
    }

    public static matchingKeys(regex: string, json: object): string[] {
        const re: RegExp = new RegExp(regex);
        return JsonUtil.keys(json).filter(key => re.test(key));
    }

    public static getProperty(json: object, propertyName: string): any {
        let rval: any = json[propertyName];
        if (rval === undefined) {
            rval = null;
        }
        return rval;
    }

    public static setProperty(json: object, propertyName: string, propertyValue: any): void {
        if (propertyValue !== null) {
            json[propertyName] = propertyValue;
        }
    }

    public static consumeProperty(json: any, propertyName: string): any {
        let rval: any = JsonUtil.getProperty(json, propertyName);
        if (rval) {
            delete json[propertyName];
        }
        return rval;
    }

    public static stringify(json: any): string {
        return JSON.stringify(json);
    }

    public static parseJSON(jsonString: string): any {
        return JSON.parse(jsonString);
    }

    public static clone(json: any): any {
        return JSON.parse(JSON.stringify(json));
    }

    public static collectionToList<T>(collection: T[]): T[] {
        if (!collection) {
            return [];
        }
        return [...collection];
    }

    public static objectNode(): any {
        return {};
    }

    public static textNode(value: string): any {
        return value;
    }

    public static arrayNode(): any {
        return [];
    }

    public static toJsonNode(value: any): any {
        return value;
    }

    // JSweet mangled name for toJsonNode(Object) overload
    public static toJsonNode$java_lang_Object(value: any): any {
        return value;
    }

    public static toArrayNode(list: any[]): any[] {
        if (list == null) {
            return null;
        }
        const array: any[] = [];
        for (let i: number = 0; i < list.length; i++) {
            const node: any = list[i];
            if (node != null) {
                array.push(node);
            }
        }
        return array;
    }

    public static toObjectNode(value: any): object {
        return value;
    }

    public static allMatch(array: any, expectedType: string): boolean {
        if (array == null || !Array.isArray(array)) {
            return false;
        }
        for (let i: number = 0; i < array.length; i++) {
            const item: any = array[i];
            if (expectedType === "string") {
                if (typeof item !== "string") return false;
            } else if (expectedType === "boolean") {
                if (typeof item !== "boolean") return false;
            } else if (expectedType === "number") {
                if (typeof item !== "number") return false;
            } else if (expectedType === "integer") {
                if (typeof item !== "number" || !Number.isInteger(item)) return false;
            } else if (expectedType === "object") {
                if (typeof item !== "object" || Array.isArray(item)) return false;
            } else if (expectedType === "any") {
                if (item == null) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static allValuesMatch(obj: any, expectedType: string): boolean {
        if (obj == null) {
            return false;
        }
        const fieldNames: string[] = JsonUtil.keys(obj);
        for (let i: number = 0; i < fieldNames.length; i++) {
            const item: any = obj[fieldNames[i]];
            if (expectedType === "string") {
                if (typeof item !== "string") return false;
            } else if (expectedType === "boolean") {
                if (typeof item !== "boolean") return false;
            } else if (expectedType === "number") {
                if (typeof item !== "number") return false;
            } else if (expectedType === "integer") {
                if (typeof item !== "number" || !Number.isInteger(item)) return false;
            } else if (expectedType === "object") {
                if (typeof item !== "object" || Array.isArray(item)) return false;
            } else if (expectedType === "any") {
                if (item == null) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static addToArray(array: Array<any>, value: any): void {
        array.push(value);
    }

    public static isString(value: any): boolean {
        if (value == null) {
            return false;
        }
        return typeof value === "string";
    }

    public static isJsonNode(value: any): boolean {
        if (value == null) {
            return false;
        }
        return true;
    }

    public static isObjectNode(value: any): boolean {
        if (value == null) {
            return false;
        }
        return typeof value === "object";
    }

    public static toString(value: any): string {
        return value;
    }

    public static isBoolean(value: any): boolean {
        if (value == null) {
            return false;
        }
        return typeof value === "boolean";
    }

    public static toBoolean(value: any): boolean {
        return value;
    }

    public static isNumber(value: any): boolean {
        if (value == null) {
            return false;
        }
        return typeof value === "number";
    }

    public static toNumber(value: any): number {
        return value;
    }

    public static toInteger(value: any): number {
        return value;
    }

    public static isObject(value: any): boolean {
        if (value == null) {
            return false;
        }
        return typeof value === "object" && !Array.isArray(value);
    }

    public static isObjectWithProperty(value: any, propertyName: string): boolean {
        if (JsonUtil.isObject(value)) {
            return value.hasOwnProperty(propertyName);
        }
        return false;
    }

    public static isObjectWithStringPropertyValue(value: any, propertyName: string, propertyValue: string): boolean {
        if (JsonUtil.isObject(value)) {
            if (value.hasOwnProperty(propertyName)) {
                const pvalue: any = value[propertyName];
                if (pvalue != null && typeof pvalue === "string") {
                    return propertyValue === pvalue;
                }
            }
        }
        return false;
    }

    public static toObject(value: any): object {
        return value;
    }

    public static isArray(value: any): boolean {
        if (value == null) {
            return false;
        }
        return Array.isArray(value);
    }

    public static toArray(value: any): any[] {
        return value;
    }

    public static toList(value: any): any[] {
        return value;
    }

}
