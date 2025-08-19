"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { Calendar, Clock, Sun, Moon } from "lucide-react"

export default function ProductExpirationCalculator() {
  const [formData, setFormData] = useState({
    productionDate: "",
    duration: "",
    unit: "days" as const,
  })

  const [result, setResult] = useState<{
    expirationDate: string
    status: "fresh" | "expiring" | "expired"
    daysRemaining: number
  } | null>(null)

  const [isDarkMode, setIsDarkMode] = useState(false)

  useEffect(() => {
    // Check for saved theme preference or default to light mode
    const savedTheme = localStorage.getItem("theme")
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches

    if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
      setIsDarkMode(true)
      document.documentElement.classList.add("dark")
    }
  }, [])

  const toggleTheme = () => {
    const newTheme = !isDarkMode
    setIsDarkMode(newTheme)

    if (newTheme) {
      document.documentElement.classList.add("dark")
      localStorage.setItem("theme", "dark")
    } else {
      document.documentElement.classList.remove("dark")
      localStorage.setItem("theme", "light")
    }
  }

  const formatDateInput = (value: string) => {
    // Remove all non-digits
    const digits = value.replace(/\D/g, "")

    // Format as DD.MM.YYYY
    if (digits.length <= 2) {
      return digits
    } else if (digits.length <= 4) {
      return `${digits.slice(0, 2)}.${digits.slice(2)}`
    } else if (digits.length <= 8) {
      return `${digits.slice(0, 2)}.${digits.slice(2, 4)}.${digits.slice(4, 8)}`
    }

    return `${digits.slice(0, 2)}.${digits.slice(2, 4)}.${digits.slice(4, 8)}`
  }

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatDateInput(e.target.value)
    setFormData({ ...formData, productionDate: formatted })
  }

  const calculateExpiration = (productionDate: string, duration: number, unit: string) => {
    const [day, month, year] = productionDate.split(".").map(Number)
    const date = new Date(year, month - 1, day)

    switch (unit) {
      case "days":
        date.setDate(date.getDate() + duration)
        break
      case "weeks":
        date.setDate(date.getDate() + duration * 7)
        break
      case "months":
        date.setMonth(date.getMonth() + duration)
        break
      case "years":
        date.setFullYear(date.getFullYear() + duration)
        break
    }

    return date.toLocaleDateString("ru-RU")
  }

  const getStatus = (expirationDate: string): { status: "fresh" | "expiring" | "expired"; daysRemaining: number } => {
    const [day, month, year] = expirationDate.split(".").map(Number)
    const expDate = new Date(year, month - 1, day)
    const today = new Date()
    const diffTime = expDate.getTime() - today.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    let status: "fresh" | "expiring" | "expired"
    if (diffDays < 0) status = "expired"
    else if (diffDays <= 7) status = "expiring"
    else status = "fresh"

    return { status, daysRemaining: diffDays }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "fresh":
        return "text-green-600 dark:text-green-400"
      case "expiring":
        return "text-yellow-600 dark:text-yellow-400"
      case "expired":
        return "text-red-600 dark:text-red-400"
      default:
        return "text-foreground"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "fresh":
        return "Свежий"
      case "expiring":
        return "Скоро истечет"
      case "expired":
        return "Истек"
      default:
        return ""
    }
  }

  useEffect(() => {
    if (formData.productionDate && formData.duration) {
      const expirationDate = calculateExpiration(
        formData.productionDate,
        Number.parseInt(formData.duration),
        formData.unit,
      )
      const { status, daysRemaining } = getStatus(expirationDate)
      setResult({ expirationDate, status, daysRemaining })
    } else {
      setResult(null)
    }
  }, [formData])

  return (
    <div className="min-h-screen bg-background transition-colors duration-200">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-6 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-foreground font-[family-name:var(--font-space-grotesk)]">
            Калькулятор срока годности
          </h1>
          <Button variant="outline" size="icon" onClick={toggleTheme} className="ml-auto bg-transparent">
            {isDarkMode ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-6 max-w-md">
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="font-[family-name:var(--font-space-grotesk)] text-card-foreground">
              Расчет срока годности
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="date" className="text-card-foreground">
                Дата производства
              </Label>
              <Input
                id="date"
                placeholder="ДД.ММ.ГГГГ"
                value={formData.productionDate}
                onChange={handleDateChange}
                maxLength={10}
                className="bg-input border-border"
              />
            </div>

            <div>
              <Label htmlFor="duration" className="text-card-foreground">
                Срок годности
              </Label>
              <Input
                id="duration"
                placeholder="Введите число"
                type="number"
                value={formData.duration}
                onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                className="bg-input border-border"
              />
            </div>

            <div>
              <Label htmlFor="unit" className="text-card-foreground">
                Единица измерения
              </Label>
              <Select value={formData.unit} onValueChange={(value: any) => setFormData({ ...formData, unit: value })}>
                <SelectTrigger className="bg-input border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="days">Дни</SelectItem>
                  <SelectItem value="weeks">Недели</SelectItem>
                  <SelectItem value="months">Месяцы</SelectItem>
                  <SelectItem value="years">Года</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {result && (
          <Card className="bg-card border-border mt-6">
            <CardContent className="p-4">
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <Calendar className="w-4 h-4 text-card-foreground" />
                  <span className="text-card-foreground">
                    Истекает: <strong>{result.expirationDate}</strong>
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-card-foreground" />
                  <span className="text-card-foreground">
                    {result.daysRemaining >= 0
                      ? `Осталось: ${result.daysRemaining} дн.`
                      : `Просрочен на: ${Math.abs(result.daysRemaining)} дн.`}
                  </span>
                </div>

                <div className={`font-medium text-lg ${getStatusColor(result.status)}`}>
                  Статус: {getStatusText(result.status)}
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {!result && formData.productionDate && formData.duration && (
          <Card className="bg-card border-border mt-6">
            <CardContent className="p-4 text-center">
              <p className="text-muted-foreground">Проверьте правильность введенных данных</p>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  )
}
