
export class LoggerUtil {

    public static info(message: string, ...args: any[]): void {
        console.info(message, ...args);
    }
    
    public static warn(message: string, ...args: any[]): void {
        console.warn(message, ...args);
    }

    public static debug(message: string, ...args: any[]): void {
        console.debug(message, ...args);
    }

}