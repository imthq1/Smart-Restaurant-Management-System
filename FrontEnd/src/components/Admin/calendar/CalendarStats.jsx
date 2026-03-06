import React, { useEffect, useState } from "react";
import { getScheduleStats } from "../../../services/calendar/calendarService";
import "../../../styles/calendar-stats.css"; 

const CalendarStats = ({ currentDate }) => {
  const [stats, setStats] = useState([]);

  const STAT_CONFIG = {
    MEETING: { label: "Meetings", className: "stat-orange" },
    MENU_UPDATE: { label: "Menu Updates", className: "stat-beige" },
    INVENTORY_CHECK: { label: "Inventory Checks", className: "stat-light" },
    EVENT: { label: "Events", className: "stat-grey" },
    TRAINING: { label: "Trainings", className: "stat-red" },
  };

  useEffect(() => {
    const fetchStats = async () => {
      if (!currentDate) return;

      const year = currentDate.getFullYear();
      const month = currentDate.getMonth();

      const from = new Date(year, month, 1).toISOString().split('T')[0] + "T00:00:00";
      const to = new Date(year, month + 1, 0).toISOString().split('T')[0] + "T23:59:59";

      const data = await getScheduleStats(from, to);
      setStats(data);
    };

    fetchStats();
  }, [currentDate]);

  return (
    <div className="calendar-stats-row">
      {stats.map((item) => {
        const config = STAT_CONFIG[item.type] || { 
            label: item.type, 
            className: "stat-default" 
        };

        return (
          <div key={item.type} className={`stat-pill ${config.className}`}>
            <span className="dot"></span>
            <span className="stat-label">
              {config.label} <span className="stat-count">({item.count})</span>
            </span>
          </div>
        );
      })}
    </div>
  );
};

export default CalendarStats;