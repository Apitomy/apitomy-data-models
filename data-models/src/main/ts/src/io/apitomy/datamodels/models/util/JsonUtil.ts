
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

    /* Get/Consume a JSON (Object) property. */
    public static getObjectProperty(json: object, propertyName: string): object {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeObjectProperty(json: object, propertyName: string): object {
        const value: object = JsonUtil.getObjectProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/Consume a JSON (Any) property. */
    public static getAnyProperty(json: object, propertyName: string): any {
        return JsonUtil.getProperty(json, propertyName);
    }
    public static consumeAnyProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getAnyProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of anys property. */
    public static getAnyArrayProperty(json: object, propertyName: string): any[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <any[]> value;
        }
        return null;
    }
    public static consumeAnyArrayProperty(json: object, propertyName: string): any[] {
        const value: any[] = JsonUtil.getAnyArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of objects property. */
    public static getObjectArrayProperty(json: object, propertyName: string): object[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <object[]> value;
        }
        return null;
    }
    public static consumeObjectArrayProperty(json: object, propertyName: string): object[] {
        const value: object[] = JsonUtil.getObjectArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of strings property. */
    public static getStringArrayProperty(json: object, propertyName: string): string[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <string[]> value;
        }
        return null;
    }
    public static consumeStringArrayProperty(json: object, propertyName: string): string[] {
        const value: string[] = JsonUtil.getStringArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of integers property. */
    public static getIntegerArrayProperty(json: object, propertyName: string): number[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <number[]> value;
        }
        return null;
    }
    public static consumeIntegerArrayProperty(json: object, propertyName: string): number[] {
        const value: number[] = JsonUtil.getIntegerArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of numbers property. */
    public static getNumberArrayProperty(json: object, propertyName: string): number[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <number[]> value;
        }
        return null;
    }
    public static consumeNumberArrayProperty(json: object, propertyName: string): number[] {
        const value: number[] = JsonUtil.getNumberArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume an array of booleans property. */
    public static getBooleanArrayProperty(json: object, propertyName: string): boolean[] {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (Array.isArray(value)) {
            return <boolean[]> value;
        }
        return null;
    }
    public static consumeBooleanArrayProperty(json: object, propertyName: string): boolean[] {
        const value: boolean[] = JsonUtil.getBooleanArrayProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/Consume a string property. */
    public static getStringProperty(json: object, propertyName: string): string {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "string") {
            return <string> value;
        }
        return null;
    }
    public static consumeStringProperty(json: object, propertyName: string): string {
        const value: string = JsonUtil.getStringProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/Consume an Integer property. */
    public static getIntegerProperty(json: object, propertyName: string): number {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "number") {
            return <number> value;
        }
        return null;
    }
    public static consumeIntegerProperty(json: object, propertyName: string): number {
        const value: number = JsonUtil.getIntegerProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/Consume a Number property. */
    public static getNumberProperty(json: object, propertyName: string): number {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "number") {
            return <number> value;
        }
        return null;
    }
    public static consumeNumberProperty(json: object, propertyName: string): number {
        const value: number = JsonUtil.getNumberProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/Consume a Boolean property. */
    public static getBooleanProperty(json: object, propertyName: string): boolean {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "boolean") {
            return <boolean> value;
        }
        return null;
    }
    public static consumeBooleanProperty(json: object, propertyName: string): boolean {
        const value: boolean = JsonUtil.getBooleanProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume a map of anys property. */
    public static getAnyMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeAnyMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getAnyMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume a map of objects property. */
    public static getObjectMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeObjectMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getObjectMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }


    /* Get/consume a map of strings property. */
    public static getStringMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeStringMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getStringMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume a map of integers property. */
    public static getIntegerMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeIntegerMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getIntegerMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume a map of numbers property. */
    public static getNumberMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeNumberMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getNumberMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }

    /* Get/consume a map of numbers property. */
    public static getBooleanMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getProperty(json, propertyName);
        if (typeof value === "object") {
            return value;
        }
        return null;
    }
    public static consumeBooleanMapProperty(json: object, propertyName: string): any {
        const value: any = JsonUtil.getBooleanMapProperty(json, propertyName);
        if (value != null && value !== undefined) {
            delete json[propertyName];
        }
        return value;
    }


    /* Set a JSON (Object) property. */
    public static setObjectProperty(json: object, propertyName: string, value: object): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a JSON (Any) property. */
    public static setAnyProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of anys property. */
    public static setAnyArrayProperty(json: object, propertyName: string, value: any[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of objects property. */
    public static setObjectArrayProperty(json: object, propertyName: string, value: object[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of strings property. */
    public static setStringArrayProperty(json: object, propertyName: string, value: string[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of integers property. */
    public static setIntegerArrayProperty(json: object, propertyName: string, value: number[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of numbers property. */
    public static setNumberArrayProperty(json: object, propertyName: string, value: number[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an array of booleans property. */
    public static setBooleanArrayProperty(json: object, propertyName: string, value: boolean[]): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a string property. */
    public static setStringProperty(json: object, propertyName: string, value: string): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set an Integer property. */
    public static setIntegerProperty(json: object, propertyName: string, value: number): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a Number property. */
    public static setNumberProperty(json: object, propertyName: string, value: number): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a Boolean property. */
    public static setBooleanProperty(json: object, propertyName: string, value: boolean): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of anys property. */
    public static setAnyMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of objects property. */
    public static setObjectMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of strings property. */
    public static setStringMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of integers property. */
    public static setIntegerMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of numbers property. */
    public static setNumberMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
    }
    /* Set a map of numbers property. */
    public static setBooleanMapProperty(json: object, propertyName: string, value: any): void {
        JsonUtil.setProperty(json, propertyName, value);
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
    
    public static cloneCollection(collection: Array<any>): Array<any> {
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

    public static addToArray(array: Array<any>, value: any): void {
        array.push(value);
    }

    public static isPropertyDefined(json: any, propertyName: string): boolean {
        return json && json[propertyName] != null && json[propertyName] != undefined;
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

    public static toJsonNode(value: any): any {
        return value;
    }

    public static toObjectNode(value: any): object {
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

    public static isObjectWithPropertyValue(value: any, propertyName: string, propertyValue: string): boolean {
        if (JsonUtil.isObject(value)) {
            // Note: intentional use of the "==" operator here.  We want to do the type conversion before comparison in this case.
            return value[propertyName] == propertyValue;
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
