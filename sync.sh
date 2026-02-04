#!/bin/bash

# Billiards Scraper - Quick Sync Script
# This script helps you sync data easily

BASE_URL="http://localhost:8080/api/sync"

echo "========================================="
echo "  Billiards Scraper - Sync Utility"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to call API and show result
call_api() {
    local endpoint=$1
    local description=$2

    echo -e "${YELLOW}➤ ${description}...${NC}"
    response=$(curl -s "${BASE_URL}${endpoint}")

    if [[ $response == *"successfully"* ]]; then
        echo -e "${GREEN}✓ ${response}${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}✗ ${response}${NC}"
        echo ""
        return 1
    fi
}

# Menu function
show_menu() {
    echo "Select sync option:"
    echo ""
    echo "  1) Full Sync (Recommended for daily updates)"
    echo "     → Syncs: clubs, players, latest tournaments, latest matches"
    echo ""
    echo "  2) Sync Latest Tournament Only"
    echo "     → Syncs: latest tournaments + latest matches"
    echo ""
    echo "  3) Sync Clubs"
    echo "  4) Sync Players"
    echo "  5) Sync Latest Tournaments (current year)"
    echo "  6) Sync Tournaments for Specific Year"
    echo "  7) Sync All Tournaments (all years - SLOW!)"
    echo "  8) Sync Latest Tournament Matches"
    echo "  9) Sync Matches for Specific Tournament (by External ID)"
    echo " 10) Sync Player Stats for Specific Tournament (by External ID)"
    echo " 11) Sync All Matches (all tournaments - VERY SLOW!)"
    echo ""
    echo "  0) Exit"
    echo ""
}

# Main loop
while true; do
show_menu
    read -p "Enter your choice [0-11]: " choice
    echo ""

    case $choice in
        1)
            echo "Starting Full Sync..."
            echo "This will sync clubs → players → latest tournaments → latest matches"
            echo ""
            call_api "/full" "Full Sync"
            ;;
        2)
            echo "Starting Latest Tournament Sync..."
            echo ""
            call_api "/tournaments/latest" "Syncing latest tournaments"
            call_api "/matches/latest" "Syncing latest tournament matches"
            ;;
        3)
            call_api "/clubs" "Syncing clubs"
            ;;
        4)
            call_api "/players" "Syncing players"
            ;;
        5)
            call_api "/tournaments/latest" "Syncing latest tournaments"
            ;;
        6)
            read -p "Enter year (e.g., 2025): " year
            call_api "/tournaments/year/${year}" "Syncing tournaments for year ${year}"
            ;;
        7)
            echo -e "${RED}WARNING: This will sync ALL tournaments for ALL years!${NC}"
            echo "This may take 30-60 minutes or more."
            read -p "Are you sure? (yes/no): " confirm
            if [ "$confirm" = "yes" ]; then
                call_api "/tournaments/all" "Syncing all tournaments"
            else
                echo "Cancelled."
                echo ""
            fi
            ;;
        8)
            call_api "/matches/latest" "Syncing latest tournament matches"
            ;;
        9)
            read -p "Enter tournament External ID (e.g., 12345): " tournamentId
            if [ -z "$tournamentId" ]; then
                echo -e "${RED}Error: External ID cannot be empty${NC}"
                echo ""
            else
                call_api "/matches/tournament/${tournamentId}" "Syncing matches for tournament ${tournamentId}"
            fi
            ;;
        10)
            read -p "Enter tournament External ID (e.g., 12345): " tournamentId
            if [ -z "$tournamentId" ]; then
                echo -e "${RED}Error: External ID cannot be empty${NC}"
                echo ""
            else
                call_api "/stats/tournament/${tournamentId}" "Syncing player stats for tournament ${tournamentId}"
            fi
            ;;
        11)
            echo -e "${RED}WARNING: This will sync ALL matches for ALL tournaments!${NC}"
            echo "This may take 1-3 hours or more."
            read -p "Are you sure? (yes/no): " confirm
            if [ "$confirm" = "yes" ]; then
                call_api "/matches/all" "Syncing all matches"
            else
                echo "Cancelled."
                echo ""
            fi
            ;;
        0)
            echo "Exiting..."
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid choice. Please try again.${NC}"
            echo ""
            ;;
    esac

    read -p "Press Enter to continue..."
    clear
done
