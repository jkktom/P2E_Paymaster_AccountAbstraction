/**
 * Utility functions for timezone-aware date handling
 */

import { format } from 'date-fns';
import { toZonedTime, fromZonedTime } from 'date-fns-tz';
import { ko } from 'date-fns/locale';

const KOREA_TIMEZONE = 'Asia/Seoul';

/**
 * Convert user input (Korean time) to UTC for backend
 * @param koreaDate Date object in Korean timezone
 * @returns Formatted UTC string for backend (yyyy-MM-ddTHH:mm:ss)
 */
export function formatDateForBackend(koreaDate: Date): string {
  // Convert from Korean timezone to UTC
  const utcDate = fromZonedTime(koreaDate, KOREA_TIMEZONE);
  return format(utcDate, 'yyyy-MM-dd\'T\'HH:mm:ss');
}

/**
 * Display date as-is (database already stores Korean time)
 * @param dateString Date string from backend (Korean time)
 * @returns Formatted Korean time string for display
 */
export function formatDateForDisplay(dateString: string | number): string {
  try {
    let date: Date;
    
    if (typeof dateString === 'number') {
      // Handle timestamp (milliseconds)
      date = new Date(dateString);
    } else if (typeof dateString === 'string') {
      // Treat as local time (Korean time from DB) - no timezone conversion
      date = new Date(dateString);
    } else {
      throw new Error('Invalid date format');
    }
    
    // Display as-is (database already stores Korean time)
    return format(date, 'yyyy년 M월 d일 HH:mm', { locale: ko });
    
  } catch (error) {
    console.error('Error formatting date:', dateString, error);
    return '날짜 형식 오류';
  }
}

/**
 * Get current date in Korean timezone
 * @returns Date object representing current time in Korea
 */
export function getCurrentKoreaDate(): Date {
  // Convert current UTC time to Korean timezone
  return toZonedTime(new Date(), KOREA_TIMEZONE);
}

/**
 * Add days to a date in Korean timezone
 * @param baseDate Base date
 * @param days Number of days to add
 * @returns New date with added days
 */
export function addDaysToKoreaDate(baseDate: Date, days: number): Date {
  const result = new Date(baseDate);
  result.setDate(result.getDate() + days);
  return result;
}

/**
 * Format date for HTML datetime-local input (Korean timezone)
 * @param date Date in Korean timezone
 * @returns String formatted as "YYYY-MM-DDTHH:mm" for datetime-local input
 */
export function formatDateForInput(date: Date): string {
  return format(date, 'yyyy-MM-dd\'T\'HH:mm');
}