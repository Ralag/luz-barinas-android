#!/usr/bin/env python3
"""
Seed Supabase with full Luz Barinas data catalog.
"""

import urllib.request
import json
import time
import re
import sys

# SUPABASE CREDENTIALS
SUPABASE_URL = "https://ikttyjojubtredehtneb.supabase.co/rest/v1"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlrdHR5am9qdWJ0cmVkZWh0bmViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwOTU4NTIsImV4cCI6MjEwNTY3MTg1Mn0.kbEerwT-EWdIlxlGWlEv7kSJ-k9AnteTe52IzxYDlXg"

def supabase_upsert(table, records):
    url = f"{SUPABASE_URL}/{table}?on_conflict=id"
    if table == "app_config":
        url = f"{SUPABASE_URL}/{table}?on_conflict=config_key"
        
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Content-Type": "application/json",
        "Prefer": "resolution=merge-duplicates"
    }
    
    body = json.dumps(records).encode("utf-8")
    req = urllib.request.Request(url, data=body, method="POST", headers=headers)
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status in (200, 201)
    except urllib.error.HTTPError as e:
        print(f"HTTPError {e.code} on {table}: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"Error upserting {table}: {e}")
        return False

# ==============================================================================
# DATA DEFINITIONS
# ==============================================================================

PAC_SLOTS = [
    {"timeLabel": "03:00 a 07:00", "startHour": 3, "endHour": 7},
    {"timeLabel": "07:00 a 11:00", "startHour": 7, "endHour": 11},
    {"timeLabel": "11:00 a 15:00", "startHour": 11, "endHour": 15},
    {"timeLabel": "15:00 a 19:00", "startHour": 15, "endHour": 19},
    {"timeLabel": "19:00 a 23:00", "startHour": 19, "endHour": 23},
    {"timeLabel": "23:00 a 03:00", "startHour": 23, "endHour": 3}
]

PAC_MATRIX = [
    ["C", "B", "A", "C", "B", "A", "C"],
    ["A", "C", "B", "A", "C", "B", "A"],
    ["B", "A", "C", "B", "A", "C", "B"],
    ["C", "B", "A", "C", "B", "A", "C"],
    ["A", "C", "B", "A", "C", "B", "A"],
    ["B", "A", "C", "B", "A", "C", "B"]
]

SECTORS_BLOQUE_A = [
    "Alto Barinas I 34,5 kV", "Obispos 34,5 kV", "Guasimito", "Centro", "Sur",
    "Industrial", "Norte", "Raúl Leoni", "Las Palmas", "Primero Diciembre",
    "Pagueycito", "Progreso", "Don Simón", "El Real", "El Tambor", "Cdad Bolivia II",
    "Mijagua 34,5 kV", "Mirí", "Urb. Las Ingenieras I, II y III", "Los Pozones",
    "Urb. Don Samuel", "Alto Barinas Norte", "Urb. Los Próceres", "Urb. La Rosaleda",
    "Urb. Vista Hermosa", "Urb. Villas del Pilar", "Urb. Campo Móvil"
]

SECTORS_BLOQUE_B = [
    "Barinitas 34,5 kV", "Parangula", "Floresta", "Centro Norte", "Los Pinos",
    "Carolina", "Borburata", "Negro Primero", "Hormiga", "Cdad Varyna",
    "Cdad Tavacare", "San Silvestre", "Sta Ines Lucia", "Libertad", "Sta. Rosa",
    "Curbati", "Ticoporo", "Capitanejo 34,5 kV", "Santa Elena kV", "Barrio 5 de Julio",
    "Mi Jardín (Sectores 1, 2, 3)", "Ciudad Varyna Los Samanes", "Ciudad Varyna Bucares",
    "Ciudad Tavacare Sector A-C", "Ciudad Tavacare Sector D-G", "Palacio del Pan / Av. 23 de Enero"
]

SECTORS_BLOQUE_C = [
    "Expresa 34,5 kV", "Alto Barinas II 34,5 kV", "Socopo I", "Bum Bum", "Socopo II",
    "Cardenera", "Corocito", "Bolivar", "Esperanza", "Torunos", "Cdad Nutria 34,5 kV",
    "Dolores", "El Paguey", "Cdad Bolivia I", "Fundacea", "Estadio", "Barrio El Carmen",
    "Urb. La Cincuentena I, II y III", "Barrio La Paz", "Urb. Barinas II", "Urb. Barinas IV",
    "Alto Barinas Sur", "Casco Central / Plaza Bolívar", "Av. Medina Jiménez"
]

SECTORS_BLOQUE_D = [
    "Alto Barinas II 34,5 kV", "Obispos 34,5 kV", "Floresta", "Los Pinos", "Industrial",
    "Cardenera", "Corocito", "El Paguey", "Cdad Bolivia I", "Mijagua 34,5 kV",
    "Caaez 34,5 kV", "Boconoito", "Otopum-Pajen", "Canagua 34,5 kV", "Libertad",
    "Sector La Caramuca", "Sector Punta Gorda", "Quebrada Seca", "El Corozo"
]

MASTER_SECTORS = [
    {"name": "Alto Barinas Norte", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel / Norte", "status": "NORMAL", "voltage": 118.5},
    {"name": "Urb. Don Samuel", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel", "status": "NORMAL", "voltage": 117.8},
    {"name": "Urb. Los Próceres", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 119.0},
    {"name": "Urb. Las Ingenieras I, II y III", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 118.2},
    {"name": "Urb. La Rosaleda", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Alto Barinas I", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Vista Hermosa", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel", "status": "NORMAL", "voltage": 117.5},
    {"name": "Urb. Villas del Pilar", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Alto Barinas I", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Campo Móvil", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Raúl Leoni", "parroquia": "R. Ignacio Méndez", "block": "A", "circuit": "Circuito Raúl Leoni", "status": "NORMAL", "voltage": 117.0},
    {"name": "Los Pozones", "parroquia": "Rómulo Betancourt", "block": "A", "circuit": "Circuito Los Pozones", "status": "NORMAL", "voltage": 116.5},
    {"name": "Las Palmas", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Las Palmas", "status": "NORMAL", "voltage": 118.0},
    {"name": "Primero de Diciembre", "parroquia": "R. Ignacio Méndez", "block": "A", "circuit": "Circuito Mijagua", "status": "NORMAL", "voltage": 117.2},
    {"name": "Zona Industrial", "parroquia": "Torunos", "block": "A", "circuit": "Circuito Industrial", "status": "NORMAL", "voltage": 119.5},
    {"name": "Obispos 34,5 kV", "parroquia": "Municipio Obispos", "block": "A", "circuit": "Subestación Obispos", "status": "NORMAL", "voltage": 118.0},
    {"name": "Guasimito", "parroquia": "Obispos", "block": "A", "circuit": "Circuito Guasimito", "status": "NORMAL", "voltage": 116.0},
    {"name": "Ciudad Bolivia II", "parroquia": "Pedraza", "block": "A", "circuit": "Circuito Pedraza", "status": "NORMAL", "voltage": 115.5},
    {"name": "Barinitas 34,5 kV", "parroquia": "Bolívar", "block": "B", "circuit": "Subestación Barinitas", "status": "NORMAL", "voltage": 118.0},
    {"name": "Ciudad Varyna Los Samanes", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Varyna", "status": "NORMAL", "voltage": 117.5},
    {"name": "Ciudad Varyna Bucares", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Varyna", "status": "NORMAL", "voltage": 117.0},
    {"name": "Ciudad Tavacare Sector A-C", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Tavacare", "status": "NORMAL", "voltage": 116.8},
    {"name": "Ciudad Tavacare Sector D-G", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Tavacare", "status": "NORMAL", "voltage": 116.5},
    {"name": "Barrio 5 de Julio", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Carolina", "status": "NORMAL", "voltage": 117.0},
    {"name": "Mi Jardín (Sectores 1, 2, 3)", "parroquia": "R. Ignacio Méndez", "block": "B", "circuit": "Circuito Hormiga", "status": "NORMAL", "voltage": 116.0},
    {"name": "Urb. La Floresta", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Floresta", "status": "NORMAL", "voltage": 118.0},
    {"name": "Los Pinos", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Los Pinos", "status": "NORMAL", "voltage": 117.5},
    {"name": "Palacio del Pan / Av. 23 de Enero", "parroquia": "El Carmen", "block": "B", "circuit": "Circuito Centro Norte", "status": "NORMAL", "voltage": 118.2},
    {"name": "Barrio Negro Primero", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Negro Primero", "status": "NORMAL", "voltage": 116.5},
    {"name": "San Silvestre", "parroquia": "San Silvestre", "block": "B", "circuit": "Circuito Foráneo", "status": "NORMAL", "voltage": 115.0},
    {"name": "Santa Rosa", "parroquia": "Santa Rosa", "block": "B", "circuit": "Circuito Santa Rosa", "status": "NORMAL", "voltage": 115.0},
    {"name": "Urb. La Cincuentena I y II", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. La Cincuentena III", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Barrio El Carmen", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Barrio La Paz", "parroquia": "Rómulo Betancourt", "block": "C", "circuit": "Circuito Esperanza", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. Barinas II", "parroquia": "Corazón de Jesús", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. Barinas IV", "parroquia": "Corazón de Jesús", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Alto Barinas Sur", "parroquia": "Alto Barinas", "block": "C", "circuit": "Circuito Alto Barinas II 34,5 kV", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Casco Central / Plaza Bolívar", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Bolívar", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Av. Medina Jiménez", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Estadio", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Socopó I", "parroquia": "Sucre", "block": "C", "circuit": "Subestación Socopó", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Bum Bum", "parroquia": "Sucre", "block": "C", "circuit": "Circuito Bum Bum", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Torunos", "parroquia": "Torunos", "block": "C", "circuit": "Circuito Torunos", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Fundacea", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Fundacea", "status": "IRREGULAR_OUTAGE", "voltage": 0.0},
    {"name": "Sector La Caramuca", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito La Caramuca", "status": "NORMAL", "voltage": 117.0},
    {"name": "Sector Punta Gorda", "parroquia": "Corazón de Jesús", "block": "D", "circuit": "Circuito Punta Gorda", "status": "NORMAL", "voltage": 116.5},
    {"name": "Quebrada Seca", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito Foráneo Sur", "status": "NORMAL", "voltage": 115.0},
    {"name": "El Corozo", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito El Corozo", "status": "NORMAL", "voltage": 116.0},
    {"name": "Boconoito", "parroquia": "San Genaro", "block": "D", "circuit": "Circuito Boconoito", "status": "NORMAL", "voltage": 116.0},
    {"name": "Canagua 34,5 kV", "parroquia": "Pedraza", "block": "D", "circuit": "Circuito Canagua", "status": "NORMAL", "voltage": 115.8}
]

COMMUNITY_SUBMISSIONS = [
    {
        "id": "sec_community_cincuentena_etapa_4",
        "name": "Urb. La Cincuentena Etapa IV",
        "parroquia": "El Carmen",
        "block": "C",
        "circuitCode": "Circuito Corocito",
        "status": "NORMAL",
        "voltage": 118.0
    }
]

CITIZEN_REPORTS = [
    {"sectorName": "Urb. Don Samuel", "hasPower": True, "reportType": "NORMAL", "voltage": 118.0, "deviceOrigin": "CitizenApp_Mobile"}
]

def main():
    now_ms = int(time.time() * 1000)
    print("=========================================================")
    print("⚡ SEEDING LUZ BARINAS DATA CATALOG TO SUPABASE")
    print("=========================================================")

    # 1. Seed PAC Schedule Document: app_config
    print("\n[1/5] Subiendo cronograma oficial PAC y matriz semanal...")
    matrix_rows = [{"cells": row} for row in PAC_MATRIX]
    pac_payload = {
        "version": now_ms,
        "updatedAt": now_ms,
        "matrixRows": matrix_rows,
        "slots": PAC_SLOTS,
        "sectorsA": SECTORS_BLOQUE_A,
        "sectorsB": SECTORS_BLOQUE_B,
        "sectorsC": SECTORS_BLOQUE_C,
        "sectorsD": SECTORS_BLOQUE_D
    }
    app_config_records = [
        {"config_key": "pac_schedule", "config_value": pac_payload},
        {"config_key": "broadcast_notice", "config_value": {
            "title": "⚡ Cronograma Oficial PAC Barinas 2026 en Operación",
            "message": "Plan de Administración de Carga activo. Usando backend Supabase.",
            "level": "INFO",
            "timestamp": now_ms,
            "active": True
        }}
    ]
    if supabase_upsert("app_config", app_config_records):
        print("  ✓ app_config actualizado con éxito.")

    # 2. Seed Master Sectors
    print(f"\n[3/5] Subiendo {len(MASTER_SECTORS)} sectores a la tabla 'sectors'...")
    sector_records = []
    for s in MASTER_SECTORS:
        clean_id = "sec_" + re.sub(r'[^a-z0-9]', '_', s["name"].lower()).strip('_')
        doc_data = {
            "id": clean_id,
            "name": s["name"],
            "parroquia": s["parroquia"],
            "circuitCode": s["circuit"],
            "rotationBlock": s["block"],
            "status": s["status"],
            "voltage": s["voltage"],
            "confirmedReportsCount": 18 if s["status"] == "SCHEDULED_OUTAGE" else 3,
            "withoutPowerPercentage": 95 if s["status"] == "SCHEDULED_OUTAGE" else 0,
            "lastUpdatedMillis": now_ms
        }
        sector_records.append(doc_data)
        
        
    if supabase_upsert("sectors", sector_records):
        print(f"  ✓ {len(sector_records)} sectores subidos.")

    # 3. Seed Community Submissions
    print(f"\n[4/5] Subiendo {len(COMMUNITY_SUBMISSIONS)} sectores comunitarios...")
    community_records = []
    for c in COMMUNITY_SUBMISSIONS:
        doc_data = {
            "id": c["id"],
            "name": c["name"],
            "parroquia": c["parroquia"],
            "municipio": "Barinas",
            "block": c["block"],
            "circuitCode": c["circuitCode"],
            "status": c["status"],
            "voltage": c["voltage"],
            "confirmedReportsCount": 1,
            "withoutPowerPercentage": 0,
            "rotationBlock": c["block"],
            "submittedAt": now_ms
        }
        community_records.append(doc_data)
        
        # also add as an active sector
        sector_doc = {
            "id": c["id"],
            "name": c["name"],
            "parroquia": c["parroquia"],
            "circuitCode": c["circuitCode"],
            "rotationBlock": c["block"],
            "status": c["status"],
            "voltage": c["voltage"],
            "confirmedReportsCount": 1,
            "withoutPowerPercentage": 0,
            "lastUpdatedMillis": now_ms
        }
        sector_records.append(sector_doc)
        
    if supabase_upsert("community_locations", community_records):
        print(f"  ✓ {len(community_records)} sectores comunitarios subidos.")
        # Upload them to sectors as well
        supabase_upsert("sectors", sector_records)

    print("\n=========================================================")
    print("✅ ¡SEMBRADO DE SUPABASE COMPLETADO CON ÉXITO!")
    print("=========================================================")

if __name__ == "__main__":
    main()
